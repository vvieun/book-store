from __future__ import annotations

import csv
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

HATCHES = ["///", "\\\\\\", "...", "xxx", "+++", "ooo"]


def bar_hatched(ax, x, height, *, width: float, label: str, hatch: str) -> None:
    ax.bar(
        x,
        height,
        width=width,
        label=label,
        facecolor="white",
        edgecolor="black",
        linewidth=0.9,
        hatch=hatch,
    )


CONFIG_LABELS = {
    "none": "без индексов",
    "simple": "простые",
    "composite_only": "составные",
    "simple_composite": "простые+составные",
    "overindexed": "избыточные",
}

ALL_QUERY_LABELS = {
    "q1_category_top_books": "q1: топ книг по категории",
    "q2_user_reviews_sorted": "q2: отзывы с сортировкой",
    "q3_user_orders_recent": "q3: заказы по дате",
    "q4_category_rating_stats": "q4: статистика рейтингов",
    "q5_user_order_history_join": "q5: история заказов (JOIN)",
    "q6_write_insert_order": "q6: пакетная вставка",
}

INDEX_COUNTS = {
    "none": 0,
    "simple": 7,
    "composite_only": 4,
    "simple_composite": 11,
    "overindexed": 20,
}


def load_rows(csv_path: Path) -> list[dict[str, str]]:
    with csv_path.open("r", encoding="utf-8") as csv_file:
        return list(csv.DictReader(csv_file))


def query_values_by_mode(rows: list[dict[str, str]]) -> tuple[list[str], dict[str, list[float]]]:
    modes = list(CONFIG_LABELS.keys())
    mode_index = {mode: i for i, mode in enumerate(modes)}
    values = {query_key: [0.0] * len(modes) for query_key in ALL_QUERY_LABELS}

    for row in rows:
        query_key = row["query_key"]
        if query_key not in values:
            continue
        values[query_key][mode_index[row["mode"]]] = float(row["avg_ms"])

    return modes, values


def build_plot(rows: list[dict[str, str]], output_path: Path) -> None:
    modes, query_values = query_values_by_mode(rows)
    x_labels = [CONFIG_LABELS[mode] for mode in modes]
    x = list(range(len(modes)))
    fig, axes = plt.subplots(1, 2, figsize=(11.0, 4.8))
    bar_width = 0.24

    for idx, query_key in enumerate(("q1_category_top_books", "q2_user_reviews_sorted", "q3_user_orders_recent")):
        x_shifted = [pos + (idx - 1) * bar_width for pos in x]
        bar_hatched(
            axes[0],
            x_shifted,
            query_values[query_key],
            width=bar_width,
            label=ALL_QUERY_LABELS[query_key],
            hatch=HATCHES[idx % len(HATCHES)],
        )
    axes[0].set_xticks(list(x), x_labels, rotation=20, ha="right")
    axes[0].set_ylabel("среднее время выполнения, мс")
    axes[0].set_title("Среднее время запросов q1--q3")
    axes[0].grid(axis="y", alpha=0.25)
    axes[0].legend(fontsize=7.5)

    for idx, query_key in enumerate(("q4_category_rating_stats", "q5_user_order_history_join", "q6_write_insert_order")):
        x_shifted = [pos + (idx - 1) * bar_width for pos in x]
        bar_hatched(
            axes[1],
            x_shifted,
            query_values[query_key],
            width=bar_width,
            label=ALL_QUERY_LABELS[query_key],
            hatch=HATCHES[idx % len(HATCHES)],
        )
    axes[1].set_xticks(list(x), x_labels, rotation=20, ha="right")
    axes[1].set_ylabel("среднее время выполнения, мс")
    axes[1].set_title("Среднее время запросов q4--q6")
    axes[1].grid(axis="y", alpha=0.25)
    axes[1].legend(fontsize=7.5)

    fig.tight_layout()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(output_path, format="pdf")


def build_dependency_plot(rows: list[dict[str, str]], output_path: Path) -> None:
    modes, query_values = query_values_by_mode(rows)
    sorted_modes = sorted(modes, key=lambda mode: INDEX_COUNTS[mode])
    x_labels = [str(INDEX_COUNTS[mode]) for mode in sorted_modes]
    x = list(range(len(sorted_modes)))

    read_mean = []
    write_mean = []
    for mode in sorted_modes:
        mode_rows = [row for row in rows if row["mode"] == mode]
        read_values = [float(row["avg_ms"]) for row in mode_rows if row["query_key"] != "q6_write_insert_order"]
        write_values = [float(row["avg_ms"]) for row in mode_rows if row["query_key"] == "q6_write_insert_order"]
        read_mean.append(sum(read_values) / len(read_values))
        write_mean.append(sum(write_values) / len(write_values))

    fig, axes = plt.subplots(1, 2, figsize=(11.0, 4.8))

    bar_width = 0.35
    left_bars = [pos - bar_width / 2 for pos in x]
    right_bars = [pos + bar_width / 2 for pos in x]
    bar_hatched(axes[0], left_bars, read_mean, width=bar_width, label="чтение (q1-q5)", hatch="///")
    bar_hatched(axes[0], right_bars, write_mean, width=bar_width, label="запись (q6)", hatch="...")
    axes[0].set_xticks(x, x_labels)
    axes[0].set_xlabel("число индексов в конфигурации")
    axes[0].set_ylabel("среднее время выполнения, мс")
    axes[0].set_title("Зависимость среднего времени от числа индексов")
    axes[0].grid(axis="y", alpha=0.25)
    axes[0].legend(fontsize=8)

    q3_vals = [query_values["q3_user_orders_recent"][modes.index(mode)] for mode in sorted_modes]
    q4_vals = [query_values["q4_category_rating_stats"][modes.index(mode)] for mode in sorted_modes]
    q5_vals = [query_values["q5_user_order_history_join"][modes.index(mode)] for mode in sorted_modes]
    bar_width = 0.24
    for idx, (label, vals) in enumerate((
        ("q3: заказы по дате", q3_vals),
        ("q4: статистика рейтингов", q4_vals),
        ("q5: история заказов (JOIN)", q5_vals),
    )):
        x_shifted = [pos + (idx - 1) * bar_width for pos in x]
        bar_hatched(axes[1], x_shifted, vals, width=bar_width, label=label, hatch=HATCHES[idx % len(HATCHES)])
    axes[1].set_xticks(x, x_labels)
    axes[1].set_xlabel("число индексов в конфигурации")
    axes[1].set_ylabel("среднее время выполнения, мс")
    axes[1].set_title("Зависимость среднего времени q3, q4, q5")
    axes[1].grid(axis="y", alpha=0.25)
    axes[1].legend(fontsize=8)

    fig.tight_layout()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(output_path, format="pdf")


def main() -> None:
    project_root = Path(__file__).resolve().parents[2]
    csv_path = project_root / "code" / "benchmarks" / "index_benchmark_results.csv"
    output_path = project_root / "report" / "diagrams" / "images" / "index_research_plots.pdf"
    dependency_output_path = project_root / "report" / "diagrams" / "images" / "index_research_dependency_plots.pdf"
    rows = load_rows(csv_path)
    build_plot(rows, output_path)
    build_dependency_plot(rows, dependency_output_path)
    print(f"График сохранен: {output_path}")
    print(f"График сохранен: {dependency_output_path}")


if __name__ == "__main__":
    main()
