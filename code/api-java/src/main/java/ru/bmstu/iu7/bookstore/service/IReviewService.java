package ru.bmstu.iu7.bookstore.service;

import ru.bmstu.iu7.bookstore.domain.Review;

import java.util.List;

public interface IReviewService {

    Review createReview(Long userId, Long bookId, Integer rating, String comment);

    List<Review> getUserReviews(Long userId);

    List<Review> getBookReviews(Long bookId);
}
