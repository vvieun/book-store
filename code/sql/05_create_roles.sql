CREATE ROLE app_admin WITH LOGIN PASSWORD 'admin_password';

GRANT USAGE ON SCHEMA public TO app_admin;
GRANT ALL PRIVILEGES ON ALL TABLES    IN SCHEMA public TO app_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_admin;
GRANT EXECUTE ON ALL FUNCTIONS        IN SCHEMA public TO app_admin;


CREATE ROLE app_manager WITH LOGIN PASSWORD 'manager_password';

GRANT USAGE ON SCHEMA public TO app_manager;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO app_manager;
GRANT USAGE  ON ALL SEQUENCES IN SCHEMA public TO app_manager;

GRANT INSERT, UPDATE, DELETE ON books, authors, publishers, categories,
      book_authors, book_categories TO app_manager;
GRANT USAGE ON SEQUENCE authors_author_id_seq,
              publishers_publisher_id_seq,
              categories_category_id_seq TO app_manager;

GRANT DELETE ON reviews TO app_manager;

GRANT EXECUTE ON FUNCTION get_popular_books_by_category(BIGINT, INTEGER) TO app_manager;
GRANT EXECUTE ON FUNCTION get_user_statistics(BIGINT)                     TO app_manager;


CREATE ROLE app_user WITH LOGIN PASSWORD 'user_password';

GRANT USAGE ON SCHEMA public TO app_user;
GRANT SELECT ON books, authors, publishers, categories,
               book_authors, book_categories TO app_user;

GRANT INSERT, UPDATE ON reviews      TO app_user;
GRANT INSERT         ON orders       TO app_user;
GRANT INSERT         ON order_items  TO app_user;
GRANT INSERT, DELETE ON wishlist     TO app_user;

GRANT INSERT, UPDATE, DELETE ON collections      TO app_user;
GRANT INSERT, DELETE          ON collection_books TO app_user;

GRANT USAGE ON SEQUENCE reviews_review_id_seq,
              orders_order_id_seq,
              order_items_order_item_id_seq,
              wishlist_wishlist_id_seq,
              collections_collection_id_seq,
              collection_books_collection_book_id_seq TO app_user;

GRANT EXECUTE ON FUNCTION calculate_recommendations(BIGINT) TO app_user;


CREATE OR REPLACE VIEW user_reviews AS
SELECT
    r.review_id,
    r.user_id,
    r.book_isbn,
    b.title       AS book_title,
    r.rating,
    r.comment,
    r.created_at
FROM reviews r
JOIN books b ON b.isbn = r.book_isbn
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
    w.book_isbn,
    b.title AS book_title,
    b.price,
    b.avg_rating,
    w.added_at
FROM wishlist w
JOIN books b ON b.isbn = w.book_isbn
WHERE w.user_id = current_setting('app.current_user_id', true)::BIGINT;

GRANT SELECT ON user_reviews, user_orders, user_wishlist TO app_user;

