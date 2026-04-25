CREATE INDEX idx_books_publisher_id         ON books(publisher_id);
CREATE INDEX idx_book_authors_book_id       ON book_authors(book_id);
CREATE INDEX idx_book_authors_author_id     ON book_authors(author_id);
CREATE INDEX idx_book_categories_book_id    ON book_categories(book_id);
CREATE INDEX idx_book_categories_cat_id     ON book_categories(category_id);
CREATE INDEX idx_reviews_user_id            ON reviews(user_id);
CREATE INDEX idx_reviews_book_id            ON reviews(book_id);
CREATE INDEX idx_orders_user_id             ON orders(user_id);
CREATE INDEX idx_order_items_order_id       ON order_items(order_id);
CREATE INDEX idx_order_items_book_id        ON order_items(book_id);
CREATE INDEX idx_wishlist_user_id           ON wishlist(user_id);
CREATE INDEX idx_wishlist_book_id           ON wishlist(book_id);
CREATE INDEX idx_reading_history_user_id    ON reading_history(user_id);
CREATE INDEX idx_reading_history_book_id    ON reading_history(book_id);

CREATE INDEX idx_books_avg_rating     ON books(avg_rating DESC);
CREATE INDEX idx_books_rating_count   ON books(rating_count DESC);
CREATE INDEX idx_orders_status        ON orders(status);
CREATE INDEX idx_orders_created_at    ON orders(created_at DESC);
CREATE INDEX idx_reviews_created_at   ON reviews(created_at DESC);

CREATE INDEX idx_books_category_rating
    ON book_categories(category_id) INCLUDE (book_id);

CREATE INDEX idx_reviews_user_rating
    ON reviews(user_id, rating);

CREATE INDEX idx_reading_history_user_date
    ON reading_history(user_id, read_at DESC);

COMMENT ON INDEX idx_books_avg_rating      IS 'Топ книг по среднему рейтингу';
COMMENT ON INDEX idx_books_category_rating IS 'Книги по категории — основа для фильтрации каталога';
COMMENT ON INDEX idx_reviews_user_rating   IS 'Оценки пользователя — основа для коллаборативной фильтрации';
COMMENT ON INDEX idx_reading_history_user_date IS 'История чтения пользователя с сортировкой по дате';
