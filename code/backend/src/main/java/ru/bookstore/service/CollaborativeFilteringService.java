package ru.bookstore.service;

import ru.bookstore.domain.Recommendation;

import java.util.List;

public interface CollaborativeFilteringService {

    List<Recommendation> getUserBasedRecommendations(Long userId, int count);
}
