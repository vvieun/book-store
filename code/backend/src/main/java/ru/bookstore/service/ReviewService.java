package ru.bookstore.service;

import ru.bookstore.domain.Review;

import java.util.List;

public interface ReviewService {

    Review createReview(Review review);

    boolean deleteReview(Long reviewId);

    List<Review> getAllReviews();

    List<Review> getUserReviews(Long userId);

    List<Review> getBookReviews(String isbn);
}
