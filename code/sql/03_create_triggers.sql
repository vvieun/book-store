CREATE OR REPLACE FUNCTION update_book_rating()
RETURNS TRIGGER AS $$
DECLARE
    v_book_isbn VARCHAR(20);
BEGIN
    v_book_isbn := COALESCE(NEW.book_isbn, OLD.book_isbn);

    UPDATE books
    SET
        avg_rating   = (
            SELECT COALESCE(AVG(rating)::DECIMAL(4,2), 0.0)
            FROM reviews
            WHERE book_isbn = v_book_isbn
        ),
        rating_count = (
            SELECT COUNT(*)
            FROM reviews
            WHERE book_isbn = v_book_isbn
        )
    WHERE isbn = v_book_isbn;

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

