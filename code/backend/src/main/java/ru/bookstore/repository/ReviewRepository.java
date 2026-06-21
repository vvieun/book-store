package ru.bookstore.repository;

import ru.bookstore.domain.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    List<Review> findByUserUserId(Long userId);

    List<Review> findByBookIsbn(String isbn);

    Long countByBookIsbn(String isbn);

    Double getAverageRatingByBookIsbn(String isbn);

    Review findByUserIdAndBookIsbn(Long userId, String isbn);

    Optional<Review> findById(Long id);

    List<Review> findAll();

    Review save(Review review);

    void delete(Review review);

    long count();
}
