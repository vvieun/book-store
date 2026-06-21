package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.RecommendationCacheRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final CollaborativeFilteringService collaborativeService;
    private final ContentBasedFilteringService contentBasedService;
    private final BookRepository bookRepository;
    private final RecommendationCacheRepository recommendationCacheRepository;
    @Value("${bookstore.recommendation.collaborative-weight:0.5}")
    private double collaborativeWeight;

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> getHybridRecommendations(Long userId, int count) {
        log.debug("Запрос гибридных рекомендаций: userId={}, count={}", userId, count);
        List<Recommendation> cached = recommendationCacheRepository.getHybridRecommendations(userId, count);
        if (cached != null) {
            log.debug("Возвращены рекомендации из кэша: userId={}, count={}", userId, count);
            return cached;
        }

        List<Recommendation> collaborative = collaborativeService.getUserBasedRecommendations(userId, count);
        List<Recommendation> contentBased = contentBasedService.getRecommendationsByUserPreferences(userId, count);
        List<Recommendation> merged = mergeAndRank(collaborative, contentBased, count);
        List<Recommendation> result = appendPopularIfNeeded(merged, count);
        recommendationCacheRepository.putHybridRecommendations(userId, count, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recommendation> getPopularRecommendations(int count) {
        log.debug("Запрос популярных рекомендаций: count={}", count);
        List<Recommendation> cached = recommendationCacheRepository.getPopularRecommendations(count);
        if (cached != null) {
            log.debug("Возвращены популярные рекомендации из кэша: count={}", count);
            return cached;
        }

        List<Recommendation> popular = bookRepository.findTopRatedBooks(0.0, count, 0).stream()
                .map(book -> new Recommendation(
                        book,
                        book.getAvgRating() != null ? book.getAvgRating() : 0.0,
                        "Popular book",
                        Recommendation.RecommendationType.CONTENT_BASED))
                .collect(Collectors.toList());
        recommendationCacheRepository.putPopularRecommendations(count, popular);
        return popular;
    }

    @Override
    public void clearRecommendationCaches() {
        log.info("Очищен кэш рекомендаций");
        recommendationCacheRepository.clearRecommendationCaches();
    }

    private List<Recommendation> mergeAndRank(List<Recommendation> list1,
                                              List<Recommendation> list2,
                                              int maxCount) {
        Map<String, Recommendation> merged = new LinkedHashMap<>();
        double normalizedCollaborativeWeight = Math.max(0.0, Math.min(1.0, collaborativeWeight));
        double contentWeight = 1.0 - normalizedCollaborativeWeight;

        for (Recommendation rec : list1) {
            merged.put(rec.getBook().getIsbn(), rec);
        }

        for (Recommendation rec : list2) {
            String isbn = rec.getBook().getIsbn();
            if (merged.containsKey(isbn)) {
                Recommendation existing = merged.get(isbn);
                existing.setScore(existing.getScore() * normalizedCollaborativeWeight + rec.getScore() * contentWeight);
                existing.setType(Recommendation.RecommendationType.HYBRID);
                existing.setReason("Based on similar users and your preferences");
            } else {
                merged.put(isbn, rec);
            }
        }

        return merged.values().stream()
                .sorted(Comparator.comparing(Recommendation::getScore).reversed())
                .limit(maxCount)
                .collect(Collectors.toList());
    }

    private List<Recommendation> appendPopularIfNeeded(List<Recommendation> personalized, int targetCount) {
        if (personalized.size() >= targetCount) {
            return personalized;
        }

        Map<String, Recommendation> merged = new LinkedHashMap<>();
        for (Recommendation recommendation : personalized) {
            if (recommendation.getBook() != null && recommendation.getBook().getIsbn() != null) {
                merged.put(recommendation.getBook().getIsbn(), recommendation);
            }
        }

        List<Recommendation> popular = bookRepository.findTopRatedBooks(0.0, targetCount * 2, 0).stream()
                .map(book -> new Recommendation(
                        book,
                        book.getAvgRating() != null ? book.getAvgRating() : 0.0,
                        "Popular fallback",
                        Recommendation.RecommendationType.CONTENT_BASED))
                .collect(Collectors.toList());

        Set<String> existingBookIds = merged.keySet();
        for (Recommendation recommendation : popular) {
            if (recommendation.getBook() == null || recommendation.getBook().getIsbn() == null) {
                continue;
            }
            String isbn = recommendation.getBook().getIsbn();
            if (existingBookIds.contains(isbn)) {
                continue;
            }
            merged.put(isbn, recommendation);
            if (merged.size() >= targetCount) {
                break;
            }
        }
        return merged.values().stream().limit(targetCount).collect(Collectors.toList());
    }
}
