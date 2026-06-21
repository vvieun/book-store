package ru.bookstore.service;

import ru.bookstore.domain.Recommendation;

import java.util.List;

public interface RecommendationService {

    List<Recommendation> getHybridRecommendations(Long userId, int count);

    List<Recommendation> getPopularRecommendations(int count);

    void clearRecommendationCaches();
}
