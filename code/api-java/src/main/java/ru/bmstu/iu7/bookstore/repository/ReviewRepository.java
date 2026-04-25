package ru.bmstu.iu7.bookstore.repository;

import ru.bmstu.iu7.bookstore.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    List<Review> findByUserUserId(Long userId);

    List<Review> findByBookBookId(Long bookId);

    Long countByBookBookId(Long bookId);

    Double getAverageRatingByBookId(Long bookId);

    Review findByUserIdAndBookId(Long userId, Long bookId);

    Optional<Review> findById(Long id);

    List<Review> findAll();

    Review save(Review review);

    void delete(Review review);

    long count();
}
