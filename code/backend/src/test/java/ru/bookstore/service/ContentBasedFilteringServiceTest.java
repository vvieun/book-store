package ru.bookstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bookstore.domain.Author;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Category;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.domain.Review;
import ru.bookstore.domain.User;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.ReviewRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentBasedFilteringServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ContentBasedFilteringServiceImpl service;

    private User user;
    private Category category;
    private Author author;
    private Book likedBook;
    private Book candidateBook;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);

        category = new Category(1L, "Классика", null);
        author = new Author(1L, "Толстой", null, "Россия");

        likedBook = new Book();
        likedBook.setIsbn("isbn-1");
        likedBook.setTitle("Книга A");
        likedBook.setPrice(BigDecimal.valueOf(300.0));
        likedBook.setAvgRating(4.5);
        likedBook.setCategories(Set.of(category));
        likedBook.setAuthors(Set.of(author));

        candidateBook = new Book();
        candidateBook.setIsbn("isbn-2");
        candidateBook.setTitle("Книга B");
        candidateBook.setPrice(BigDecimal.valueOf(400.0));
        candidateBook.setAvgRating(4.0);
        candidateBook.setCategories(Set.of(category));
        candidateBook.setAuthors(Set.of(author));
    }

    private Review buildReview(Book book, int rating) {
        Review r = new Review();
        r.setUser(user);
        r.setBook(book);
        r.setRating(rating);
        return r;
    }

    @Test
    void testGetRecommendations_NoReviews_ReturnsEmpty() {
        when(reviewRepository.findByUserUserId(1L)).thenReturn(Collections.emptyList());

        List<Recommendation> result = service.getRecommendationsByUserPreferences(1L, 10);

        assertTrue(result.isEmpty());
        verifyNoInteractions(bookRepository);
    }

    @Test
    void testGetRecommendations_OnlyLowRatings_ReturnsEmpty() {
        Review r = buildReview(likedBook, 2);
        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of(r));
        when(bookRepository.findAll()).thenReturn(List.of(candidateBook));

        List<Recommendation> result = service.getRecommendationsByUserPreferences(1L, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRecommendations_WithHighRating_ReturnsRecommendations() {
        Review r = buildReview(likedBook, 5);
        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of(r));
        when(bookRepository.findAll()).thenReturn(List.of(likedBook, candidateBook));
        when(bookRepository.findById("isbn-2")).thenReturn(Optional.of(candidateBook));

        List<Recommendation> result = service.getRecommendationsByUserPreferences(1L, 10);

        assertFalse(result.isEmpty());
        verify(bookRepository).findAll();
    }

    @Test
    void testGetRecommendations_UnrelatedBook_NotIncluded() {
        Book unrelated = new Book();
        unrelated.setIsbn("isbn-3");
        unrelated.setTitle("Другой жанр");
        unrelated.setCategories(new HashSet<>());
        unrelated.setAuthors(new HashSet<>());

        Review r = buildReview(likedBook, 5);
        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of(r));
        when(bookRepository.findAll()).thenReturn(List.of(likedBook, unrelated));

        List<Recommendation> result = service.getRecommendationsByUserPreferences(1L, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetRecommendations_LimitZero_ReturnsEmpty() {
        Review r = buildReview(likedBook, 5);
        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of(r));
        when(bookRepository.findAll()).thenReturn(List.of(candidateBook));

        List<Recommendation> result = service.getRecommendationsByUserPreferences(1L, 0);

        assertTrue(result.isEmpty());
    }
}
