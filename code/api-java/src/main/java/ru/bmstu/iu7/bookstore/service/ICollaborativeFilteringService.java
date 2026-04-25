package ru.bmstu.iu7.bookstore.service;

import ru.bmstu.iu7.bookstore.dto.BookRecommendation;

import java.util.List;

public interface ICollaborativeFilteringService {

    List<BookRecommendation> getUserBasedRecommendations(Long userId, int count);
}
