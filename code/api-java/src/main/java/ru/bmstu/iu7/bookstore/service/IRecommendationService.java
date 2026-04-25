package ru.bmstu.iu7.bookstore.service;

import ru.bmstu.iu7.bookstore.dto.BookRecommendation;

import java.util.List;

public interface IRecommendationService {

    List<BookRecommendation> getHybridRecommendations(Long userId, int count);

    List<BookRecommendation> getPopularRecommendations(int count);
}
