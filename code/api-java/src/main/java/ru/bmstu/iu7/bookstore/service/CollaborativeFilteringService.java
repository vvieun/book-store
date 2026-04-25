package ru.bmstu.iu7.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bmstu.iu7.bookstore.dto.BookRecommendation;
import ru.bmstu.iu7.bookstore.entity.Book;
import ru.bmstu.iu7.bookstore.entity.Review;
import ru.bmstu.iu7.bookstore.entity.User;
import ru.bmstu.iu7.bookstore.repository.BookRepository;
import ru.bmstu.iu7.bookstore.repository.ReviewRepository;
import ru.bmstu.iu7.bookstore.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborativeFilteringService implements ICollaborativeFilteringService {
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<BookRecommendation> getUserBasedRecommendations(Long userId, int count) {
        log.debug("Calculating user-based collaborative filtering recommendations for user {}", userId);
        
        List<Review> userReviews = reviewRepository.findByUserUserId(userId);
        if (userReviews.isEmpty()) {
            log.debug("User {} has no reviews, returning empty recommendations", userId);
            return Collections.emptyList();
        }
        
        List<User> similarUsers = findSimilarUsers(userId, userReviews, 10);
        if (similarUsers.isEmpty()) {
            log.debug("No similar users found for user {}", userId);
            return Collections.emptyList();
        }
        
        Map<Long, Double> bookScores = calculateBookScores(userReviews, similarUsers);
        
        return bookScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(count)
                .map(entry -> toRecommendation(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private List<User> findSimilarUsers(Long targetUserId, List<Review> targetUserReviews, int maxCount) {
        Map<Long, Integer> targetRatings = targetUserReviews.stream()
                .collect(Collectors.toMap(
                        r -> r.getBook().getBookId(),
                        Review::getRating
                ));
        
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
            
            Map<Long, Integer> userRatings = userReviews.stream()
                    .collect(Collectors.toMap(
                            r -> r.getBook().getBookId(),
                            Review::getRating
                    ));
            
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
    
    private Double calculatePearsonCorrelation(Map<Long, Integer> ratings1, Map<Long, Integer> ratings2) {
        Set<Long> commonBooks = new HashSet<>(ratings1.keySet());
        commonBooks.retainAll(ratings2.keySet());
        
        if (commonBooks.isEmpty()) {
            return null;
        }
        if (commonBooks.size() < 3) {
            double avgDiff = commonBooks.stream()
                    .mapToDouble(bookId -> Math.abs(ratings1.get(bookId) - ratings2.get(bookId)))
                    .average()
                    .orElse(4.0);
            return Math.max(0.0, 1.0 - (avgDiff / 4.0));
        }
        
        double sum1 = 0, sum2 = 0, sum1Sq = 0, sum2Sq = 0, pSum = 0;
        int n = commonBooks.size();
        
        for (Long bookId : commonBooks) {
            double rating1 = ratings1.get(bookId);
            double rating2 = ratings2.get(bookId);
            
            sum1 += rating1;
            sum2 += rating2;
            sum1Sq += rating1 * rating1;
            sum2Sq += rating2 * rating2;
            pSum += rating1 * rating2;
        }
        
        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - sum1 * sum1 / n) * (sum2Sq - sum2 * sum2 / n));
        
        if (den == 0) {
            return 0.0;
        }
        
        return num / den;
    }
    
    private Map<Long, Double> calculateBookScores(List<Review> targetUserReviews, List<User> similarUsers) {
        Map<Long, Double> bookScores = new HashMap<>();
        Map<Long, Double> similaritySum = new HashMap<>();
        
        Map<Long, Integer> targetRatings = targetUserReviews.stream()
                .collect(Collectors.toMap(r -> r.getBook().getBookId(), Review::getRating));
        Set<Long> readBooks = targetUserReviews.stream()
                .map(r -> r.getBook().getBookId())
                .collect(Collectors.toSet());
        
        for (User similarUser : similarUsers) {
            List<Review> reviews = reviewRepository.findByUserUserId(similarUser.getUserId());
            Map<Long, Integer> similarUserRatings = reviews.stream()
                    .collect(Collectors.toMap(r -> r.getBook().getBookId(), Review::getRating));
            
            Double similarity = calculatePearsonCorrelation(targetRatings, similarUserRatings);
            if (similarity == null || similarity <= 0) {
                continue;
            }
            
            for (Review review : reviews) {
                Long bookId = review.getBook().getBookId();
                
                if (readBooks.contains(bookId)) {
                    continue;
                }
                
                bookScores.merge(bookId, similarity * review.getRating(), Double::sum);
                similaritySum.merge(bookId, similarity, Double::sum);
            }
        }
        
        for (Map.Entry<Long, Double> entry : bookScores.entrySet()) {
            Double simSum = similaritySum.get(entry.getKey());
            if (simSum != null && simSum > 0) {
                bookScores.put(entry.getKey(), entry.getValue() / simSum);
            }
        }
        
        return bookScores;
    }
    
    private BookRecommendation toRecommendation(Long bookId, Double score) {
        Book book = bookRepository.findByIdWithDetails(bookId);
        if (book != null) {
            BookRecommendation recommendation = new BookRecommendation();
            recommendation.setBook(DomainMapper.toDomain(book));
            recommendation.setScore(score);
            recommendation.setType(BookRecommendation.RecommendationType.COLLABORATIVE);
            recommendation.setReason("Based on similar users' preferences");
            return recommendation;
        }
        return null;
    }
}
