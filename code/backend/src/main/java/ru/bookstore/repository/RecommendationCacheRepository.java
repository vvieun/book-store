package ru.bookstore.repository;

import ru.bookstore.domain.Recommendation;

import java.util.List;

public interface RecommendationCacheRepository {

    List<Recommendation> getHybridRecommendations(Long userId, int count);

    void putHybridRecommendations(Long userId, int count, List<Recommendation> recommendations);

    List<Recommendation> getPopularRecommendations(int count);

    void putPopularRecommendations(int count, List<Recommendation> recommendations);

    void clearRecommendationCaches();
}
