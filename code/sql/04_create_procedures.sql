CREATE OR REPLACE FUNCTION calculate_recommendations(p_user_id BIGINT)
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER := 0;
BEGIN
    DELETE FROM recommendations WHERE user_id = p_user_id;

    WITH user_reviews AS (
        SELECT book_id, rating
        FROM reviews
        WHERE user_id = p_user_id
    ),
    similar_users AS (
        SELECT
            r.user_id,
            CORR(ur.rating::NUMERIC, r.rating::NUMERIC) AS similarity
        FROM reviews r
        JOIN user_reviews ur ON r.book_id = ur.book_id
        WHERE r.user_id <> p_user_id
        GROUP BY r.user_id
        HAVING COUNT(*) >= 3
           AND CORR(ur.rating::NUMERIC, r.rating::NUMERIC) > 0.3
        ORDER BY similarity DESC
        LIMIT 10
    ),
    candidate_books AS (
        SELECT DISTINCT r.book_id
        FROM reviews r
        JOIN similar_users su ON r.user_id = su.user_id
        WHERE r.book_id NOT IN (SELECT book_id FROM user_reviews)
    ),
    predicted_scores AS (
        SELECT
            cb.book_id,
            SUM(su.similarity * r.rating) / NULLIF(SUM(ABS(su.similarity)), 0) AS score
        FROM candidate_books cb
        JOIN reviews r       ON cb.book_id = r.book_id
        JOIN similar_users su ON r.user_id = su.user_id
        GROUP BY cb.book_id
        HAVING SUM(ABS(su.similarity)) > 0
    )
    INSERT INTO recommendations (user_id, book_id, score, created_at)
    SELECT
        p_user_id,
        book_id,
        score,
        CURRENT_TIMESTAMP
    FROM predicted_scores
    ORDER BY score DESC
    LIMIT 20;

    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_popular_books_by_category(
    p_category_id BIGINT,
    p_limit       INTEGER DEFAULT 10
)
RETURNS TABLE (
    book_id      BIGINT,
    title        VARCHAR,
    avg_rating   DECIMAL,
    rating_count INTEGER,
    price        DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        b.book_id,
        b.title,
        b.avg_rating,
        b.rating_count,
        b.price
    FROM books b
    JOIN book_categories bc ON bc.book_id = b.book_id
    WHERE bc.category_id = p_category_id
      AND b.rating_count > 0
    ORDER BY b.avg_rating DESC, b.rating_count DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_user_statistics(p_user_id BIGINT)
RETURNS TABLE (
    total_reviews    INTEGER,
    avg_user_rating  DECIMAL,
    total_orders     INTEGER,
    total_spent      DECIMAL,
    favorite_category VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COUNT(DISTINCT r.review_id)::INTEGER        AS total_reviews,
        AVG(r.rating)::DECIMAL(3,2)                 AS avg_user_rating,
        COUNT(DISTINCT o.order_id)::INTEGER          AS total_orders,
        COALESCE(SUM(o.total_amount), 0)::DECIMAL(10,2) AS total_spent,
        (
            SELECT c.name
            FROM reviews r2
            JOIN book_categories bc ON bc.book_id = r2.book_id
            JOIN categories c       ON c.category_id = bc.category_id
            WHERE r2.user_id = p_user_id
            GROUP BY c.category_id, c.name
            ORDER BY COUNT(*) DESC
            LIMIT 1
        ) AS favorite_category
    FROM reviews r
    LEFT JOIN orders o ON o.user_id = p_user_id
                      AND o.status = 'DELIVERED'
    WHERE r.user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;


CREATE TABLE IF NOT EXISTS recommendations (
    recommendation_id BIGSERIAL PRIMARY KEY,
    user_id           BIGINT        NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    book_id           BIGINT        NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    score             DECIMAL(8,6)  NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_id)
);

COMMENT ON FUNCTION calculate_recommendations(BIGINT)
    IS 'User-Based CF: корреляция Пирсона по отзывам; результат записывается в таблицу recommendations';
COMMENT ON FUNCTION get_popular_books_by_category(BIGINT, INTEGER)
    IS 'Топ книг категории по среднему рейтингу';
COMMENT ON FUNCTION get_user_statistics(BIGINT)
    IS 'Сводная статистика пользователя: отзывы, заказы, любимая категория';
