CREATE OR REPLACE FUNCTION update_book_rating()
RETURNS TRIGGER AS $$
DECLARE
    v_book_id BIGINT;
BEGIN
    v_book_id := COALESCE(NEW.book_id, OLD.book_id);

    UPDATE books
    SET
        avg_rating   = (
            SELECT COALESCE(AVG(rating)::DECIMAL(3,2), 0.0)
            FROM reviews
            WHERE book_id = v_book_id
        ),
        rating_count = (
            SELECT COUNT(*)
            FROM reviews
            WHERE book_id = v_book_id
        )
    WHERE book_id = v_book_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_review_insert
AFTER INSERT ON reviews
FOR EACH ROW
EXECUTE FUNCTION update_book_rating();

CREATE TRIGGER trg_review_update
AFTER UPDATE ON reviews
FOR EACH ROW
EXECUTE FUNCTION update_book_rating();

CREATE TRIGGER trg_review_delete
AFTER DELETE ON reviews
FOR EACH ROW
EXECUTE FUNCTION update_book_rating();


CREATE OR REPLACE FUNCTION create_reading_history_on_review()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO reading_history (user_id, book_id, read_at, progress_percent)
    VALUES (NEW.user_id, NEW.book_id, NEW.created_at, 100)
    ON CONFLICT DO NOTHING;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_reading_history_on_review
AFTER INSERT ON reviews
FOR EACH ROW
EXECUTE FUNCTION create_reading_history_on_review();


COMMENT ON FUNCTION update_book_rating()
    IS 'Пересчёт avg_rating и rating_count книги при изменении отзывов';
COMMENT ON FUNCTION create_reading_history_on_review()
    IS 'Автоматическое создание записи истории чтения при первом отзыве пользователя на книгу';
