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
    name        VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE authors (
    author_id  BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    biography  TEXT,
    country    VARCHAR(100)
);

CREATE TABLE books (
    book_id          BIGSERIAL PRIMARY KEY,
    title            VARCHAR(500) NOT NULL,
    isbn             VARCHAR(20)  UNIQUE,
    price            DECIMAL(10,2) NOT NULL CHECK (price > 0),
    description      TEXT,
    pages            INTEGER CHECK (pages > 0),
    publication_date DATE,
    avg_rating       DECIMAL(3,2) DEFAULT 0.0,
    rating_count     INTEGER DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    publisher_id     BIGINT REFERENCES publishers(publisher_id) ON DELETE SET NULL
);

CREATE TABLE book_authors (
    book_id   BIGINT NOT NULL REFERENCES books(book_id)   ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES authors(author_id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE book_categories (
    book_id     BIGINT NOT NULL REFERENCES books(book_id)      ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(category_id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, category_id)
);

CREATE TABLE reviews (
    review_id  BIGSERIAL PRIMARY KEY,
    user_id    BIGINT  NOT NULL REFERENCES users(user_id)  ON DELETE CASCADE,
    book_id    BIGINT  NOT NULL REFERENCES books(book_id)  ON DELETE CASCADE,
    rating     INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment    TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_id)
);

CREATE TABLE orders (
    order_id     BIGSERIAL PRIMARY KEY,
    user_id      BIGINT        NOT NULL REFERENCES users(user_id),
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.0,
    status       VARCHAR(50)   NOT NULL
                     CHECK (status IN ('PENDING', 'PROCESSING', 'DELIVERED', 'CANCELLED')),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    order_item_id BIGSERIAL PRIMARY KEY,
    order_id      BIGINT        NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    book_id       BIGINT        NOT NULL REFERENCES books(book_id),
    quantity      INTEGER       NOT NULL CHECK (quantity > 0),
    price         DECIMAL(10,2) NOT NULL CHECK (price > 0)
);

CREATE TABLE wishlist (
    wishlist_id BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(user_id)  ON DELETE CASCADE,
    book_id     BIGINT NOT NULL REFERENCES books(book_id)  ON DELETE CASCADE,
    added_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_id)
);

CREATE TABLE reading_history (
    history_id       BIGSERIAL PRIMARY KEY,
    user_id          BIGINT  NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    book_id          BIGINT  NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    read_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress_percent INTEGER  DEFAULT 0
                         CHECK (progress_percent >= 0 AND progress_percent <= 100)
);

COMMENT ON TABLE users           IS 'Зарегистрированные пользователи системы';
COMMENT ON TABLE publishers      IS 'Издатели книг';
COMMENT ON TABLE categories      IS 'Категории (жанры) книг';
COMMENT ON TABLE authors         IS 'Авторы книг';
COMMENT ON TABLE books           IS 'Книги каталога';
COMMENT ON TABLE book_authors    IS 'Связь книг и авторов (многие-ко-многим)';
COMMENT ON TABLE book_categories IS 'Связь книг и категорий (многие-ко-многим)';
COMMENT ON TABLE reviews         IS 'Отзывы и оценки пользователей к книгам';
COMMENT ON TABLE orders          IS 'Заказы пользователей';
COMMENT ON TABLE order_items     IS 'Позиции заказа';
COMMENT ON TABLE wishlist        IS 'Список желаемых книг пользователя';
COMMENT ON TABLE reading_history IS 'История чтения пользователя';
