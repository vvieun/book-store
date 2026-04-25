-- Вспомогательные функции для генерации случайных данных

CREATE OR REPLACE FUNCTION random_date(start_date DATE, end_date DATE)
RETURNS DATE AS $$
BEGIN
    RETURN start_date + (random() * (end_date - start_date))::INTEGER;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION random_timestamp(start_date DATE, end_date DATE)
RETURNS TIMESTAMP AS $$
BEGIN
    RETURN start_date
        + (random() * (end_date - start_date + 1))::INTEGER * INTERVAL '1 day'
        + (random() * 24)::INTEGER * INTERVAL '1 hour'
        + (random() * 60)::INTEGER * INTERVAL '1 minute';
END;
$$ LANGUAGE plpgsql;


-- Проверка количества строк во всех таблицах после загрузки данных

SELECT 'users'            AS table_name, COUNT(*) AS row_count FROM users
UNION ALL
SELECT 'publishers',       COUNT(*) FROM publishers
UNION ALL
SELECT 'categories',       COUNT(*) FROM categories
UNION ALL
SELECT 'authors',          COUNT(*) FROM authors
UNION ALL
SELECT 'books',            COUNT(*) FROM books
UNION ALL
SELECT 'book_authors',     COUNT(*) FROM book_authors
UNION ALL
SELECT 'book_categories',  COUNT(*) FROM book_categories
UNION ALL
SELECT 'reviews',          COUNT(*) FROM reviews
UNION ALL
SELECT 'orders',           COUNT(*) FROM orders
UNION ALL
SELECT 'order_items',      COUNT(*) FROM order_items
UNION ALL
SELECT 'wishlist',         COUNT(*) FROM wishlist
UNION ALL
SELECT 'reading_history',  COUNT(*) FROM reading_history
UNION ALL
SELECT 'recommendations',  COUNT(*) FROM recommendations
ORDER BY table_name;
