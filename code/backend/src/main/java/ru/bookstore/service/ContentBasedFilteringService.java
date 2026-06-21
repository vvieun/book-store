package ru.bookstore.service;

import ru.bookstore.domain.Recommendation;

import java.util.List;

public interface ContentBasedFilteringService {

    List<Recommendation> getRecommendationsByUserPreferences(Long userId, int count);
}
