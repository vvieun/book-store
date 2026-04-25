package ru.bmstu.iu7.bookstore.service;

import ru.bmstu.iu7.bookstore.dto.BookRecommendation;

import java.util.List;

public interface IContentBasedFilteringService {

    List<BookRecommendation> getRecommendationsByUserPreferences(Long userId, int count);
}
