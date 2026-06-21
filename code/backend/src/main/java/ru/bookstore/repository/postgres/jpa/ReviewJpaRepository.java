package ru.bookstore.repository.postgres.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bookstore.repository.postgres.model.Review;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    List<Review> findByUserUserId(Long userId);

    List<Review> findByBookIsbn(String isbn);

    Long countByBookIsbn(String isbn);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.isbn = :isbn")
    Double getAverageRatingByBookIsbn(@Param("isbn") String isbn);

    @Query("SELECT r FROM Review r WHERE r.user.userId = :userId AND r.book.isbn = :isbn")
    Review findByUserIdAndBookIsbn(@Param("userId") Long userId, @Param("isbn") String isbn);
}
