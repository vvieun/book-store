package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.domain.Review;
import ru.bookstore.domain.User;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.ReviewRepository;
import ru.bookstore.repository.UserRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborativeFilteringServiceImpl implements CollaborativeFilteringService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> getUserBasedRecommendations(Long userId, int count) {
        log.debug("Calculating collaborative recommendations for user {}", userId);

        List<Review> userReviews = reviewRepository.findByUserUserId(userId);
        if (userReviews.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> similarUsers = findSimilarUsers(userId, userReviews, 10);
        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> bookScores = calculateBookScores(userId, userReviews, similarUsers);

        return bookScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(count)
                .map(entry -> toRecommendation(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<User> findSimilarUsers(Long targetUserId, List<Review> targetReviews, int maxCount) {
        Map<String, Integer> targetRatings = targetReviews.stream()
                .collect(Collectors.toMap(r -> r.getBook().getIsbn(), Review::getRating));

        List<User> allUsers = userRepository.findAll();
        Map<Long, Double> similarities = new HashMap<>();

        for (User user : allUsers) {
            if (user.getUserId().equals(targetUserId)) {
                continue;
            }

            List<Review> userReviews = reviewRepository.findByUserUserId(user.getUserId());
            if (userReviews.isEmpty()) {
                continue;
            }

            Map<String, Integer> userRatings = userReviews.stream()
                    .collect(Collectors.toMap(r -> r.getBook().getIsbn(), Review::getRating));

            Double similarity = calculatePearsonCorrelation(targetRatings, userRatings);
            if (similarity != null && similarity > 0.3) {
                similarities.put(user.getUserId(), similarity);
            }
        }

        return similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxCount)
                .map(entry -> userRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Double calculatePearsonCorrelation(Map<String, Integer> ratings1, Map<String, Integer> ratings2) {
        Set<String> commonBooks = new HashSet<>(ratings1.keySet());
        commonBooks.retainAll(ratings2.keySet());

        if (commonBooks.isEmpty()) {
            return null;
        }
        if (commonBooks.size() < 3) {
            double avgDiff = commonBooks.stream()
                    .mapToDouble(isbn -> Math.abs(ratings1.get(isbn) - ratings2.get(isbn)))
                    .average()
                    .orElse(4.0);
            return Math.max(0.0, 1.0 - (avgDiff / 4.0));
        }

        double sum1 = 0;
        double sum2 = 0;
        double sum1Sq = 0;
        double sum2Sq = 0;
        double pSum = 0;
        int n = commonBooks.size();

        for (String isbn : commonBooks) {
            double r1 = ratings1.get(isbn);
            double r2 = ratings2.get(isbn);
            sum1 += r1;
            sum2 += r2;
            sum1Sq += r1 * r1;
            sum2Sq += r2 * r2;
            pSum += r1 * r2;
        }

        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - sum1 * sum1 / n) * (sum2Sq - sum2 * sum2 / n));
        return den == 0 ? 0.0 : num / den;
    }

    private Map<String, Double> calculateBookScores(Long targetUserId,
                                                   List<Review> targetReviews,
                                                   List<User> similarUsers) {
        Map<String, Integer> targetRatings = targetReviews.stream()
                .collect(Collectors.toMap(r -> r.getBook().getIsbn(), Review::getRating));
        Set<String> readBooks = targetRatings.keySet();

        Map<String, Double> bookScores = new HashMap<>();
        Map<String, Double> similaritySum = new HashMap<>();

        for (User similarUser : similarUsers) {
            List<Review> reviews = reviewRepository.findByUserUserId(similarUser.getUserId());
            Map<String, Integer> similarUserRatings = reviews.stream()
                    .collect(Collectors.toMap(r -> r.getBook().getIsbn(), Review::getRating));

            Double similarity = calculatePearsonCorrelation(targetRatings, similarUserRatings);
            if (similarity == null || similarity <= 0) {
                continue;
            }

            for (Review review : reviews) {
                String isbn = review.getBook().getIsbn();
                if (readBooks.contains(isbn)) {
                    continue;
                }
                bookScores.merge(isbn, similarity * review.getRating(), Double::sum);
                similaritySum.merge(isbn, similarity, Double::sum);
            }
        }

        for (Map.Entry<String, Double> entry : bookScores.entrySet()) {
            Double simSum = similaritySum.get(entry.getKey());
            if (simSum != null && simSum > 0) {
                bookScores.put(entry.getKey(), entry.getValue() / simSum);
            }
        }
        return bookScores;
    }

    private Recommendation toRecommendation(String isbn, Double score) {
        Book book = bookRepository.findById(isbn).orElse(null);
        if (book == null) {
            return null;
        }
        Recommendation rec = new Recommendation();
        rec.setBook(book);
        rec.setScore(score);
        rec.setType(Recommendation.RecommendationType.COLLABORATIVE);
        rec.setReason("Based on similar users' preferences");
        return rec;
    }
}
