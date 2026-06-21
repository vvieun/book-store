package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.domain.Review;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.ReviewRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentBasedFilteringServiceImpl implements ContentBasedFilteringService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> getRecommendationsByUserPreferences(Long userId, int count) {
        log.debug("Calculating content-based recommendations for user {}", userId);

        List<Review> userReviews = reviewRepository.findByUserUserId(userId);
        if (userReviews.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Double> categoryScores = new HashMap<>();
        Map<Long, Double> authorScores = new HashMap<>();

        for (Review review : userReviews) {
            if (review.getRating() >= 4) {
                Book book = review.getBook();
                book.getCategories().forEach(cat ->
                        categoryScores.merge(cat.getCategoryId(), 1.0, Double::sum));
                book.getAuthors().forEach(author ->
                        authorScores.merge(author.getAuthorId(), 1.0, Double::sum));
            }
        }

        Set<String> readBooks = userReviews.stream()
                .map(r -> r.getBook().getIsbn())
                .collect(Collectors.toSet());

        List<Book> allBooks = bookRepository.findAll();
        Map<String, Double> bookScores = new HashMap<>();

        for (Book book : allBooks) {
            if (readBooks.contains(book.getIsbn())) {
                continue;
            }

            double score = 0.0;
            for (var category : book.getCategories()) {
                score += categoryScores.getOrDefault(category.getCategoryId(), 0.0);
            }
            for (var author : book.getAuthors()) {
                score += authorScores.getOrDefault(author.getAuthorId(), 0.0) * 1.5;
            }
            if (book.getAvgRating() != null && book.getAvgRating() > 0) {
                score *= (1 + book.getAvgRating() / 10.0);
            }
            if (score > 0) {
                bookScores.put(book.getIsbn(), score);
            }
        }

        return bookScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(count)
                .map(entry -> toRecommendation(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Recommendation toRecommendation(String isbn, Double score) {
        return bookRepository.findById(isbn)
                .map(book -> {
                    Recommendation rec = new Recommendation();
                    rec.setBook(book);
                    rec.setScore(score);
                    rec.setType(Recommendation.RecommendationType.CONTENT_BASED);
                    rec.setReason("Based on your favorite genres and authors");
                    return rec;
                }).orElse(null);
    }
}
