#!/usr/bin/env python3
"""
Расширенное исследование влияния индексов на производительность БД.

Сценарий:
1) приводит данные к минимально репрезентативному объему;
2) сравнивает 5 конфигураций индексов;
3) измеряет 6 типовых запросов (5 чтение + 1 запись);
4) снимает метрики avg/median/p95 и план выполнения EXPLAIN ANALYZE;
5) сохраняет результаты в CSV/JSON.
"""

from __future__ import annotations

import csv
import json
import random
import statistics
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import psycopg2
from psycopg2.extensions import connection as PgConnection


DB_CONFIG = {
    "host": "localhost",
    "port": 5433,
    "database": "bookstore_db",
    "user": "postgres",
    "password": "postgres",
}

WARMUP_RUNS = 20
MEASURE_RUNS = 250
WRITE_BATCH_SIZE = 10
BENCHMARK_ROUNDS = 3


DROP_ALL_SQL = [
    "DROP INDEX IF EXISTS idx_book_categories_cat_id",
    "DROP INDEX IF EXISTS idx_reviews_user_id",
    "DROP INDEX IF EXISTS idx_orders_user_id",
    "DROP INDEX IF EXISTS idx_orders_created_at",
    "DROP INDEX IF EXISTS idx_books_avg_rating",
    "DROP INDEX IF EXISTS idx_order_items_order_id",
    "DROP INDEX IF EXISTS idx_books_publisher_id",
    "DROP INDEX IF EXISTS idx_books_category_rating",
    "DROP INDEX IF EXISTS idx_reviews_user_rating",
    "DROP INDEX IF EXISTS idx_orders_user_status_created",
    "DROP INDEX IF EXISTS idx_reviews_book_rating",
    "DROP INDEX IF EXISTS idx_books_price",
    "DROP INDEX IF EXISTS idx_reviews_created_at_extra",
    "DROP INDEX IF EXISTS idx_orders_total_amount",
    "DROP INDEX IF EXISTS idx_orders_status_extra",
    "DROP INDEX IF EXISTS idx_orders_created_status_total",
    "DROP INDEX IF EXISTS idx_orders_user_total_amount",
    "DROP INDEX IF EXISTS idx_order_items_book_isbn_extra",
    "DROP INDEX IF EXISTS idx_order_items_price",
    "DROP INDEX IF EXISTS idx_order_items_quantity",
]

CREATE_SIMPLE_SQL = [
    "CREATE INDEX IF NOT EXISTS idx_book_categories_cat_id ON book_categories(category_id)",
    "CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id)",
    "CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id)",
    "CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC)",
    "CREATE INDEX IF NOT EXISTS idx_books_avg_rating ON books(avg_rating DESC)",
    "CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id)",
    "CREATE INDEX IF NOT EXISTS idx_books_publisher_id ON books(publisher_id)",
]

CREATE_COMPOSITE_SQL = [
    "CREATE INDEX IF NOT EXISTS idx_books_category_rating ON book_categories(category_id) INCLUDE (book_isbn)",
    "CREATE INDEX IF NOT EXISTS idx_reviews_user_rating ON reviews(user_id, rating)",
    "CREATE INDEX IF NOT EXISTS idx_orders_user_status_created ON orders(user_id, status, created_at DESC)",
    "CREATE INDEX IF NOT EXISTS idx_reviews_book_rating ON reviews(book_isbn, rating DESC)",
]


@dataclass
class QueryCase:
    key: str
    description: str
    sql: str
    params: tuple[Any, ...]
    is_write: bool = False


def run_sql_batch(conn: PgConnection, commands: list[str]) -> None:
    with conn.cursor() as cur:
        for cmd in commands:
            cur.execute(cmd)
    conn.commit()


def reset_indexes(conn: PgConnection, mode: str) -> None:
    run_sql_batch(conn, DROP_ALL_SQL)
    if mode == "none":
        pass
    elif mode == "simple":
        run_sql_batch(conn, CREATE_SIMPLE_SQL)
    elif mode == "composite_only":
        run_sql_batch(conn, CREATE_COMPOSITE_SQL)
    elif mode == "simple_composite":
        run_sql_batch(conn, CREATE_SIMPLE_SQL + CREATE_COMPOSITE_SQL)
    elif mode == "overindexed":
        extra = [
            "CREATE INDEX IF NOT EXISTS idx_books_price ON books(price)",
            "CREATE INDEX IF NOT EXISTS idx_reviews_created_at_extra ON reviews(created_at)",
            "CREATE INDEX IF NOT EXISTS idx_orders_total_amount ON orders(total_amount)",
            "CREATE INDEX IF NOT EXISTS idx_orders_status_extra ON orders(status)",
            "CREATE INDEX IF NOT EXISTS idx_orders_created_status_total ON orders(created_at DESC, status, total_amount)",
            "CREATE INDEX IF NOT EXISTS idx_orders_user_total_amount ON orders(user_id, total_amount)",
            "CREATE INDEX IF NOT EXISTS idx_order_items_book_isbn_extra ON order_items(book_isbn)",
            "CREATE INDEX IF NOT EXISTS idx_order_items_price ON order_items(price)",
            "CREATE INDEX IF NOT EXISTS idx_order_items_quantity ON order_items(quantity)",
        ]
        run_sql_batch(conn, CREATE_SIMPLE_SQL + CREATE_COMPOSITE_SQL + extra)
    else:
        raise ValueError(f"Unknown mode: {mode}")


def ensure_dataset_scale(conn: PgConnection) -> None:
    """
    Доводит объем данных до более репрезентативного уровня для измерений.
    """
    rng = random.Random(42)
    with conn.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM users")
        users_count = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM reviews")
        reviews_count = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM orders")
        orders_count = cur.fetchone()[0]

        target_users = 1000
        target_reviews = 25000
        target_orders = 30000

        if users_count < target_users:
            to_add = target_users - users_count
            for i in range(to_add):
                suffix = users_count + i + 1
                cur.execute(
                    """
                    INSERT INTO users(username, email, password_hash, role, created_at)
                    VALUES (%s, %s, %s, 'CUSTOMER', NOW())
                    ON CONFLICT DO NOTHING
                    """,
                    (
                        f"bench_user_{suffix}",
                        f"bench_user_{suffix}@bench.local",
                        "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                    ),
                )

        if reviews_count < target_reviews:
            cur.execute(
                """
                INSERT INTO reviews(user_id, book_isbn, rating, comment, created_at)
                SELECT u.user_id,
                       b.isbn,
                       1 + (abs(hashtext(u.user_id::text || ':' || b.isbn)) % 5),
                       NULL,
                       NOW() - ((abs(hashtext((u.user_id::text || b.isbn))) % 365) * INTERVAL '1 day')
                FROM users u
                CROSS JOIN books b
                WHERE u.username LIKE 'bench_user_%'
                ON CONFLICT (user_id, book_isbn) DO NOTHING
                """
            )

        if orders_count < target_orders:
            cur.execute("SELECT ARRAY_AGG(isbn) FROM books")
            all_books = cur.fetchone()[0] or []
            cur.execute("SELECT user_id FROM users WHERE username LIKE 'bench_user_%'")
            bench_users = [r[0] for r in cur.fetchall()]

            if all_books and bench_users:
                missing = target_orders - orders_count
                for _ in range(max(0, missing)):
                    user_id = rng.choice(bench_users)
                    book_isbn = rng.choice(all_books)
                    status = rng.choice(["PENDING", "CONFIRMED", "DELIVERED", "CANCELLED"])
                    price = round(rng.uniform(200, 1200), 2)
                    cur.execute(
                        """
                        INSERT INTO orders(user_id, total_amount, status, created_at)
                        VALUES (%s, %s, %s, NOW() - (%s * INTERVAL '1 day'))
                        RETURNING order_id
                        """,
                        (user_id, price, status, rng.randint(0, 365)),
                    )
                    order_id = cur.fetchone()[0]
                    cur.execute(
                        """
                        INSERT INTO order_items(order_id, book_isbn, quantity, price)
                        VALUES (%s, %s, 1, %s)
                        """,
                        (order_id, book_isbn, price),
                    )

    conn.commit()


def choose_parameters(conn: PgConnection) -> dict[str, int]:
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT user_id
            FROM reviews
            GROUP BY user_id
            ORDER BY COUNT(*) DESC
            LIMIT 1
            """
        )
        user_id = cur.fetchone()[0]

        cur.execute(
            """
            SELECT category_id
            FROM book_categories
            GROUP BY category_id
            ORDER BY COUNT(*) DESC
            LIMIT 1
            """
        )
        category_id = cur.fetchone()[0]

        cur.execute(
            """
            SELECT user_id
            FROM orders
            GROUP BY user_id
            ORDER BY COUNT(*) DESC
            LIMIT 1
            """
        )
        orders_user_id = cur.fetchone()[0]

        cur.execute(
            """
            SELECT isbn
            FROM books
            ORDER BY avg_rating DESC
            LIMIT 1
            """
        )
        write_book_isbn = cur.fetchone()[0]

        cur.execute("SELECT price FROM books WHERE isbn = %s", (write_book_isbn,))
        write_book_price = float(cur.fetchone()[0])

    return {
        "user_id": user_id,
        "category_id": category_id,
        "orders_user_id": orders_user_id,
        "write_book_isbn": write_book_isbn,
        "write_book_price": write_book_price,
    }


def build_queries(params: dict[str, int]) -> list[QueryCase]:
    user_id = params["user_id"]
    orders_user_id = params["orders_user_id"]
    category_id = params["category_id"]
    write_book_isbn = params["write_book_isbn"]
    write_book_price = params["write_book_price"]
    return [
        QueryCase(
            key="q1_category_top_books",
            description="Топ книг по категории",
            sql=(
                "SELECT b.isbn, b.avg_rating "
                "FROM books b "
                "JOIN book_categories bc ON bc.book_isbn = b.isbn "
                "WHERE bc.category_id = %s "
                "ORDER BY b.avg_rating DESC "
                "LIMIT 10"
            ),
            params=(category_id,),
        ),
        QueryCase(
            key="q2_user_reviews_sorted",
            description="Отзывы пользователя с сортировкой по рейтингу",
            sql=(
                "SELECT book_isbn, rating "
                "FROM reviews "
                "WHERE user_id = %s "
                "ORDER BY rating DESC"
            ),
            params=(user_id,),
        ),
        QueryCase(
            key="q3_user_orders_recent",
            description="Заказы пользователя по дате",
            sql=(
                "SELECT order_id, total_amount, status "
                "FROM orders "
                "WHERE user_id = %s "
                "ORDER BY created_at DESC"
            ),
            params=(orders_user_id,),
        ),
        QueryCase(
            key="q4_category_rating_stats",
            description="Агрегированная статистика рейтингов категории",
            sql=(
                "SELECT bc.category_id, COUNT(r.review_id), AVG(r.rating) "
                "FROM book_categories bc "
                "JOIN reviews r ON r.book_isbn = bc.book_isbn "
                "WHERE bc.category_id = %s "
                "GROUP BY bc.category_id"
            ),
            params=(category_id,),
        ),
        QueryCase(
            key="q5_user_order_history_join",
            description="История заказов пользователя с join по позициям",
            sql=(
                "SELECT o.order_id, o.status, oi.book_isbn, oi.price "
                "FROM orders o "
                "JOIN order_items oi ON oi.order_id = o.order_id "
                "WHERE o.user_id = %s "
                "ORDER BY o.created_at DESC "
                "LIMIT 50"
            ),
            params=(orders_user_id,),
        ),
        QueryCase(
            key="q6_write_insert_order",
            description="Запись: вставка заказов и позиций (с rollback)",
            sql="",
            params=(orders_user_id, write_book_isbn, write_book_price),
            is_write=True,
        ),
    ]


def run_write_once(cur: Any, user_id: int, book_isbn: str, price: Any) -> None:
    cur.execute("SAVEPOINT bench_sp")
    for _ in range(WRITE_BATCH_SIZE):
        cur.execute(
            """
            INSERT INTO orders(user_id, total_amount, status, created_at)
            VALUES (%s, 999.99, 'PENDING', NOW())
            RETURNING order_id
            """,
            (user_id,),
        )
        order_id = cur.fetchone()[0]
        cur.execute(
            """
            INSERT INTO order_items(order_id, book_isbn, quantity, price)
            VALUES (%s, %s, 1, %s)
            """,
            (order_id, book_isbn, price),
        )
    cur.execute("ROLLBACK TO SAVEPOINT bench_sp")


def measure_query(conn: PgConnection, query: QueryCase) -> dict[str, float]:
    timings_ms: list[float] = []
    with conn.cursor() as cur:
        for _ in range(WARMUP_RUNS):
            if query.is_write:
                run_write_once(cur, query.params[0], query.params[1], query.params[2])
            else:
                cur.execute(query.sql, query.params)
                cur.fetchall()

        for _ in range(MEASURE_RUNS):
            t0 = time.perf_counter()
            if query.is_write:
                run_write_once(cur, query.params[0], query.params[1], query.params[2])
            else:
                cur.execute(query.sql, query.params)
                cur.fetchall()
            dt_ms = (time.perf_counter() - t0) * 1000.0
            timings_ms.append(dt_ms)

    return {
        "avg_ms": statistics.mean(timings_ms),
        "median_ms": statistics.median(timings_ms),
        "p95_ms": statistics.quantiles(timings_ms, n=100)[94],
        "min_ms": min(timings_ms),
        "max_ms": max(timings_ms),
    }


def extract_plan_stats(node: dict[str, Any], acc: dict[str, int]) -> None:
    node_type = node.get("Node Type", "")
    if node_type == "Seq Scan":
        acc["seq_scan"] += 1
    if "Index" in node_type:
        acc["index_scan"] += 1
    for child in node.get("Plans", []) or []:
        extract_plan_stats(child, acc)


def explain_query(conn: PgConnection, query: QueryCase) -> dict[str, Any]:
    if query.is_write:
        return {"plan_summary": "write_query_no_explain"}
    with conn.cursor() as cur:
        cur.execute(
            "EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) " + query.sql,
            query.params,
        )
        plan_json = cur.fetchone()[0][0]
    acc = {"seq_scan": 0, "index_scan": 0}
    extract_plan_stats(plan_json["Plan"], acc)
    return {
        "execution_time_ms": float(plan_json["Execution Time"]),
        "planning_time_ms": float(plan_json["Planning Time"]),
        "seq_scan_nodes": acc["seq_scan"],
        "index_scan_nodes": acc["index_scan"],
    }


def main() -> None:
    out_dir = Path(__file__).resolve().parents[1] / "benchmarks"
    out_dir.mkdir(parents=True, exist_ok=True)

    csv_path = out_dir / "index_benchmark_results.csv"
    json_path = out_dir / "index_benchmark_results.json"
    explain_json_path = out_dir / "index_benchmark_explain.json"

    conn = psycopg2.connect(**DB_CONFIG)
    conn.autocommit = False

    try:
        ensure_dataset_scale(conn)
        params = choose_parameters(conn)
        queries = build_queries(params)

        modes = ["none", "simple", "composite_only", "simple_composite", "overindexed"]
        round_rows: list[dict[str, Any]] = []
        explain_rows: list[dict[str, Any]] = []

        for round_no in range(1, BENCHMARK_ROUNDS + 1):
            round_modes = modes[:]
            random.Random(2026 + round_no).shuffle(round_modes)
            print(f"[ИНФО] Раунд {round_no}/{BENCHMARK_ROUNDS}: {', '.join(round_modes)}")

            for mode in round_modes:
                print(f"[ИНФО] Применение режима индексов: {mode}")
                reset_indexes(conn, mode)

                with conn.cursor() as cur:
                    cur.execute("ANALYZE books")
                    cur.execute("ANALYZE book_categories")
                    cur.execute("ANALYZE reviews")
                    cur.execute("ANALYZE orders")
                    cur.execute("ANALYZE order_items")
                conn.commit()

                for q in queries:
                    stats = measure_query(conn, q)
                    row = {
                        "round": round_no,
                        "mode": mode,
                        "query_key": q.key,
                        "query_description": q.description,
                        **stats,
                    }
                    round_rows.append(row)
                    if round_no == BENCHMARK_ROUNDS:
                        explain = explain_query(conn, q)
                        explain_rows.append(
                            {
                                "mode": mode,
                                "query_key": q.key,
                                "query_description": q.description,
                                **explain,
                            }
                        )
                    print(
                        f"  - {q.key}: среднее={stats['avg_ms']:.4f} мс, "
                        f"p95={stats['p95_ms']:.4f} мс"
                    )

        all_rows: list[dict[str, Any]] = []
        for mode in modes:
            for q in queries:
                matching_rows = [
                    row for row in round_rows
                    if row["mode"] == mode and row["query_key"] == q.key
                ]
                all_rows.append(
                    {
                        "mode": mode,
                        "query_key": q.key,
                        "query_description": q.description,
                        "avg_ms": statistics.mean(row["avg_ms"] for row in matching_rows),
                        "median_ms": statistics.mean(row["median_ms"] for row in matching_rows),
                        "p95_ms": statistics.mean(row["p95_ms"] for row in matching_rows),
                        "min_ms": min(row["min_ms"] for row in matching_rows),
                        "max_ms": max(row["max_ms"] for row in matching_rows),
                    }
                )

        with csv_path.open("w", newline="", encoding="utf-8") as f:
            writer = csv.DictWriter(
                f,
                fieldnames=[
                    "mode",
                    "query_key",
                    "query_description",
                    "avg_ms",
                    "median_ms",
                    "p95_ms",
                    "min_ms",
                    "max_ms",
                ],
            )
            writer.writeheader()
            writer.writerows(all_rows)

        with json_path.open("w", encoding="utf-8") as f:
            json.dump(
                {
                    "db_config": {k: DB_CONFIG[k] for k in ("host", "port", "database", "user")},
                    "params": params,
                    "warmup_runs": WARMUP_RUNS,
                    "measure_runs": MEASURE_RUNS,
                    "write_batch_size": WRITE_BATCH_SIZE,
                    "benchmark_rounds": BENCHMARK_ROUNDS,
                    "results": all_rows,
                    "round_results": round_rows,
                },
                f,
                ensure_ascii=False,
                indent=2,
            )

        with explain_json_path.open("w", encoding="utf-8") as f:
            json.dump(explain_rows, f, ensure_ascii=False, indent=2)

        print(f"[ОК] Результаты сохранены: {csv_path}")
        print(f"[ОК] Результаты сохранены: {json_path}")
        print(f"[ОК] Результаты сохранены: {explain_json_path}")

    finally:
        conn.close()


if __name__ == "__main__":
    main()
