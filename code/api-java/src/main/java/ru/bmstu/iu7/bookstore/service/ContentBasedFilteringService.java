package ru.bmstu.iu7.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bmstu.iu7.bookstore.dto.BookRecommendation;
import ru.bmstu.iu7.bookstore.entity.Book;
import ru.bmstu.iu7.bookstore.entity.Review;
import ru.bmstu.iu7.bookstore.repository.BookRepository;
import ru.bmstu.iu7.bookstore.repository.ReviewRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentBasedFilteringService implements IContentBasedFilteringService {
    
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<BookRecommendation> getRecommendationsByUserPreferences(Long userId, int count) {
        log.debug("Calculating content-based recommendations for user {}", userId);
        
        List<Review> userReviews = reviewRepository.findByUserUserId(userId);
        if (userReviews.isEmpty()) {
            return Collections.emptyList();
        }
        
        Map<Long, Double> categoryScores = new HashMap<>();
        Map<Long, Double> authorScores = new HashMap<>();
        
        for (Review review : userReviews) {
            if (review.getRating() >= 4) {
                Book book = review.getBook();
                book.getCategories().forEach(cat -> 
                    categoryScores.merge(cat.getCategoryId(), 1.0, Double::sum));
                book.getAuthors().forEach(author -> 
                    authorScores.merge(author.getAuthorId(), 1.0, Double::sum));
            }
        }
        
        Set<Long> readBooks = userReviews.stream()
                .map(r -> r.getBook().getBookId())
                .collect(Collectors.toSet());
        
        List<Book> allBooks = bookRepository.findAll();
        Map<Long, Double> bookScores = new HashMap<>();
        
        for (Book book : allBooks) {
            if (readBooks.contains(book.getBookId())) {
                continue;
            }
            
            double score = 0.0;
            
            for (var category : book.getCategories()) {
                score += categoryScores.getOrDefault(category.getCategoryId(), 0.0);
            }
            
            for (var author : book.getAuthors()) {
                score += authorScores.getOrDefault(author.getAuthorId(), 0.0) * 1.5;
            }
            
            if (book.getAvgRating() != null && book.getAvgRating() > 0) {
                score *= (1 + book.getAvgRating() / 10.0);
            }
            
            if (score > 0) {
                bookScores.put(book.getBookId(), score);
            }
        }
        
        return bookScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(count)
                .map(entry -> toRecommendation(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private BookRecommendation toRecommendation(Long bookId, Double score) {
        return bookRepository.findById(bookId)
                .map(book -> {
                    BookRecommendation recommendation = new BookRecommendation();
                    recommendation.setBook(DomainMapper.toDomain(book));
                    recommendation.setScore(score);
                    recommendation.setType(BookRecommendation.RecommendationType.CONTENT_BASED);
                    recommendation.setReason("Based on your favorite genres and authors");
                    return recommendation;
                })
                .orElse(null);
    }
}
