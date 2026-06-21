package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Review;
import ru.bookstore.domain.User;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.RecommendationCacheRepository;
import ru.bookstore.repository.ReviewRepository;
import ru.bookstore.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final RecommendationCacheRepository recommendationCacheRepository;

    @Override
    @Transactional
    public Review createReview(Review review) {
        if (review == null || review.getUser() == null || review.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Review user is required");
        }
        if (review.getBook() == null || review.getBook().getIsbn() == null || review.getBook().getIsbn().isBlank()) {
            throw new IllegalArgumentException("Review book is required");
        }

        Long userId = review.getUser().getUserId();
        String isbn = review.getBook().getIsbn();
        log.debug("Creating review: userId={}, isbn={}, rating={}", userId, isbn, review.getRating());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + isbn));

        Review existing = reviewRepository.findByUserIdAndBookIsbn(user.getUserId(), book.getIsbn());
        Review reviewToSave;
        if (existing == null) {
            review.setUser(user);
            review.setBook(book);
            reviewToSave = review;
        } else {
            existing.setRating(review.getRating());
            existing.setComment(review.getComment());
            reviewToSave = existing;
        }

        Review saved = reviewRepository.save(reviewToSave);
        updateBookRating(book.getIsbn());
        recommendationCacheRepository.clearRecommendationCaches();
        return saved;
    }

    @Override
    @Transactional
    public boolean deleteReview(Long reviewId) {
        if (reviewId == null) {
            return false;
        }

        return reviewRepository.findById(reviewId).map(review -> {
            String isbn = review.getBook().getIsbn();
            reviewRepository.delete(review);
            updateBookRating(isbn);
            recommendationCacheRepository.clearRecommendationCaches();
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getBookReviews(String isbn) {
        return reviewRepository.findByBookIsbn(isbn);
    }

    private void updateBookRating(String isbn) {
        bookRepository.findById(isbn).ifPresent(book -> {
            Double avg = reviewRepository.getAverageRatingByBookIsbn(isbn);
            Long count = reviewRepository.countByBookIsbn(isbn);
            book.setAvgRating(avg != null ? avg : 0.0);
            book.setRatingCount(count != null ? count.intValue() : 0);
            bookRepository.save(book);
        });
    }
}
