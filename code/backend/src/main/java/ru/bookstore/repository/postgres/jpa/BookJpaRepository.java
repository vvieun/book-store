package ru.bookstore.repository.postgres.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bookstore.repository.postgres.model.Book;

import java.util.List;

public interface BookJpaRepository extends JpaRepository<Book, String> {

    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.authors " +
            "LEFT JOIN FETCH b.categories " +
            "LEFT JOIN FETCH b.publisher " +
            "WHERE b.isbn = :isbn")
    Book findByIdWithDetails(@Param("isbn") String isbn);

    @Query("SELECT DISTINCT b FROM Book b JOIN b.categories c WHERE c.categoryId = :categoryId")
    List<Book> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT b FROM Book b WHERE b.avgRating >= :minRating ORDER BY b.avgRating DESC, b.ratingCount DESC")
    List<Book> findTopRatedBooks(@Param("minRating") Double minRating, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchBooks(@Param("query") String query, Pageable pageable);
}
