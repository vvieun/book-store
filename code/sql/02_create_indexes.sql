CREATE INDEX idx_books_publisher_id          ON books(publisher_id);
CREATE INDEX idx_book_authors_book_isbn      ON book_authors(book_isbn);
CREATE INDEX idx_book_authors_author_id      ON book_authors(author_id);
CREATE INDEX idx_book_categories_book_isbn   ON book_categories(book_isbn);
CREATE INDEX idx_book_categories_category_id ON book_categories(category_id);
CREATE INDEX idx_reviews_user_id             ON reviews(user_id);
CREATE INDEX idx_reviews_book_isbn           ON reviews(book_isbn);
CREATE INDEX idx_orders_user_id              ON orders(user_id);
CREATE INDEX idx_order_items_order_id        ON order_items(order_id);
CREATE INDEX idx_order_items_book_isbn       ON order_items(book_isbn);
CREATE INDEX idx_wishlist_user_id            ON wishlist(user_id);
CREATE INDEX idx_wishlist_book_isbn          ON wishlist(book_isbn);
CREATE INDEX idx_collections_owner_user_id   ON collections(owner_user_id);
CREATE INDEX idx_collection_books_coll_id    ON collection_books(collection_id);
CREATE INDEX idx_collection_books_book_isbn  ON collection_books(book_isbn);
CREATE INDEX idx_recommendations_user_id     ON recommendations(user_id);

CREATE INDEX idx_books_avg_rating     ON books(avg_rating DESC);
CREATE INDEX idx_books_rating_count   ON books(rating_count DESC);
CREATE INDEX idx_orders_status        ON orders(status);
CREATE INDEX idx_orders_created_at    ON orders(created_at DESC);
CREATE INDEX idx_reviews_created_at   ON reviews(created_at DESC);

CREATE INDEX idx_books_category_rating
    ON book_categories(category_id) INCLUDE (book_isbn);

CREATE INDEX idx_reviews_user_rating
    ON reviews(user_id, rating);
