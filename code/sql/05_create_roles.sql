CREATE ROLE app_admin WITH LOGIN PASSWORD 'admin_password';

GRANT ALL PRIVILEGES ON ALL TABLES    IN SCHEMA public TO app_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_admin;
GRANT EXECUTE ON ALL FUNCTIONS        IN SCHEMA public TO app_admin;


CREATE ROLE app_manager WITH LOGIN PASSWORD 'manager_password';

GRANT SELECT ON ALL TABLES IN SCHEMA public TO app_manager;
GRANT USAGE  ON ALL SEQUENCES IN SCHEMA public TO app_manager;

GRANT INSERT, UPDATE, DELETE ON books, authors, publishers, categories,
      book_authors, book_categories TO app_manager;
GRANT USAGE ON SEQUENCE books_book_id_seq,
              authors_author_id_seq,
              publishers_publisher_id_seq,
              categories_category_id_seq TO app_manager;

GRANT DELETE ON reviews TO app_manager;

GRANT EXECUTE ON FUNCTION get_popular_books_by_category(BIGINT, INTEGER) TO app_manager;
GRANT EXECUTE ON FUNCTION get_user_statistics(BIGINT)                     TO app_manager;


CREATE ROLE app_user WITH LOGIN PASSWORD 'user_password';

GRANT SELECT ON books, authors, publishers, categories,
               book_authors, book_categories TO app_user;

GRANT INSERT, UPDATE ON reviews  TO app_user;
GRANT INSERT          ON orders   TO app_user;
GRANT INSERT          ON order_items TO app_user;
GRANT INSERT, DELETE  ON wishlist TO app_user;
GRANT INSERT, UPDATE  ON reading_history TO app_user;

GRANT USAGE ON SEQUENCE reviews_review_id_seq,
              orders_order_id_seq,
              order_items_order_item_id_seq,
              wishlist_wishlist_id_seq,
              reading_history_history_id_seq TO app_user;

GRANT EXECUTE ON FUNCTION calculate_recommendations(BIGINT) TO app_user;


CREATE OR REPLACE VIEW user_reviews AS
SELECT
    r.review_id,
    r.user_id,
    r.book_id,
    b.title       AS book_title,
    r.rating,
    r.comment,
    r.created_at
FROM reviews r
JOIN books b ON b.book_id = r.book_id
WHERE r.user_id = current_setting('app.current_user_id', true)::BIGINT;

CREATE OR REPLACE VIEW user_orders AS
SELECT
    o.order_id,
    o.user_id,
    o.total_amount,
    o.status,
    o.created_at
FROM orders o
WHERE o.user_id = current_setting('app.current_user_id', true)::BIGINT;

CREATE OR REPLACE VIEW user_wishlist AS
SELECT
    w.wishlist_id,
    w.user_id,
    w.book_id,
    b.title AS book_title,
    b.price,
    b.avg_rating,
    w.added_at
FROM wishlist w
JOIN books b ON b.book_id = w.book_id
WHERE w.user_id = current_setting('app.current_user_id', true)::BIGINT;

GRANT SELECT ON user_reviews, user_orders, user_wishlist TO app_user;


COMMENT ON ROLE app_admin   IS 'Технический администратор: полный доступ к БД';
COMMENT ON ROLE app_manager IS 'Менеджер/модератор: управление каталогом и модерация отзывов';
COMMENT ON ROLE app_user    IS 'Зарегистрированный покупатель: доступ к каталогу и собственным данным';
