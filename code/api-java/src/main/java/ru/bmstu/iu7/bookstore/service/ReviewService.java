package ru.bmstu.iu7.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bmstu.iu7.bookstore.domain.Review;
import ru.bmstu.iu7.bookstore.entity.Book;
import ru.bmstu.iu7.bookstore.entity.User;
import ru.bmstu.iu7.bookstore.repository.BookRepository;
import ru.bmstu.iu7.bookstore.repository.ReviewRepository;
import ru.bmstu.iu7.bookstore.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public Review createReview(Long userId, Long bookId, Integer rating, String comment) {
        log.debug("Creating review: userId={}, bookId={}, rating={}", userId, bookId, rating);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        ru.bmstu.iu7.bookstore.entity.Review existing = reviewRepository.findByUserIdAndBookId(userId, bookId);
        if (existing != null) {
            existing.setRating(rating);
            existing.setComment(comment);
            ru.bmstu.iu7.bookstore.entity.Review updated = reviewRepository.save(existing);
            updateBookRating(bookId);
            return DomainMapper.toDomain(updated);
        }

        ru.bmstu.iu7.bookstore.entity.Review review = new ru.bmstu.iu7.bookstore.entity.Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);

        ru.bmstu.iu7.bookstore.entity.Review saved = reviewRepository.save(review);

        updateBookRating(bookId);

        return DomainMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserUserId(userId).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getBookReviews(Long bookId) {
        return reviewRepository.findByBookBookId(bookId).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    private void updateBookRating(Long bookId) {
        bookRepository.findById(bookId).ifPresent(book -> {
            Double avg = reviewRepository.getAverageRatingByBookId(bookId);
            Long count = reviewRepository.countByBookBookId(bookId);
            book.setAvgRating(avg != null ? avg : 0.0);
            book.setRatingCount(count != null ? count.intValue() : 0);
            bookRepository.save(book);
        });
    }
}
