CREATE TABLE users (
    user_id       BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100)  NOT NULL UNIQUE,
    email         VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    role          VARCHAR(20)   NOT NULL DEFAULT 'CUSTOMER'
                      CHECK (role IN ('CUSTOMER','MODERATOR','ADMIN')),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE publishers (
    publisher_id BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    website VARCHAR(255)
);

CREATE TABLE categories (
    category_id BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE authors (
    author_id  BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    biography  TEXT,
    country    VARCHAR(100)
);

CREATE TABLE books (
    isbn             VARCHAR(20)  PRIMARY KEY,
    title            VARCHAR(500) NOT NULL,
    price            DECIMAL(10,2) NOT NULL CHECK (price > 0),
    description      TEXT,
    pages            INTEGER CHECK (pages IS NULL OR pages > 0),
    publication_date DATE,
    avg_rating       DECIMAL(4,2) DEFAULT 0.0,
    rating_count     INTEGER DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    publisher_id     BIGINT REFERENCES publishers(publisher_id) ON DELETE SET NULL
);

CREATE TABLE book_authors (
    book_isbn VARCHAR(20) NOT NULL REFERENCES books(isbn) ON DELETE CASCADE,
    author_id BIGINT      NOT NULL REFERENCES authors(author_id) ON DELETE CASCADE,
    PRIMARY KEY (book_isbn, author_id)
);

CREATE TABLE book_categories (
    book_isbn    VARCHAR(20) NOT NULL REFERENCES books(isbn) ON DELETE CASCADE,
    category_id  BIGINT NOT NULL REFERENCES categories(category_id) ON DELETE CASCADE,
    PRIMARY KEY (book_isbn, category_id)
);

CREATE TABLE reviews (
    review_id  BIGSERIAL PRIMARY KEY,
    user_id    BIGINT  NOT NULL REFERENCES users(user_id)  ON DELETE CASCADE,
    book_isbn  VARCHAR(20) NOT NULL REFERENCES books(isbn)  ON DELETE CASCADE,
    rating     INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment    TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_isbn)
);

CREATE TABLE orders (
    order_id     BIGSERIAL PRIMARY KEY,
    user_id      BIGINT        NOT NULL REFERENCES users(user_id),
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    status       VARCHAR(50)   NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN (
                         'PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'
                     )),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    order_item_id BIGSERIAL PRIMARY KEY,
    order_id      BIGINT        NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    book_isbn     VARCHAR(20)   NOT NULL REFERENCES books(isbn),
    quantity      INTEGER       NOT NULL CHECK (quantity > 0),
    price         DECIMAL(10,2) NOT NULL CHECK (price > 0)
);

CREATE TABLE wishlist (
    wishlist_id BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(user_id)  ON DELETE CASCADE,
    book_isbn   VARCHAR(20) NOT NULL REFERENCES books(isbn)  ON DELETE CASCADE,
    added_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_isbn)
);

CREATE TABLE collections (
    collection_id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT        NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE collection_books (
    collection_book_id BIGSERIAL PRIMARY KEY,
    collection_id      BIGINT       NOT NULL REFERENCES collections(collection_id) ON DELETE CASCADE,
    book_isbn          VARCHAR(20)  NOT NULL REFERENCES books(isbn) ON DELETE CASCADE,
    added_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (collection_id, book_isbn)
);

CREATE TABLE recommendations (
    recommendation_id BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    book_isbn         VARCHAR(20)  NOT NULL REFERENCES books(isbn) ON DELETE CASCADE,
    score             DECIMAL(8,6) NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_isbn)
);

