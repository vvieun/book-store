package ru.bmstu.iu7.bookstore.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bmstu.iu7.bookstore.entity.Review;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    List<Review> findByUserUserId(Long userId);

    List<Review> findByBookBookId(Long bookId);

    Long countByBookBookId(Long bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.bookId = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);

    @Query("SELECT r FROM Review r WHERE r.user.userId = :userId AND r.book.bookId = :bookId")
    Review findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
