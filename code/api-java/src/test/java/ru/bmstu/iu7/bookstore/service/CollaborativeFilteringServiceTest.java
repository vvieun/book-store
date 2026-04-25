package ru.bmstu.iu7.bookstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bmstu.iu7.bookstore.dto.BookRecommendation;
import ru.bmstu.iu7.bookstore.entity.Book;
import ru.bmstu.iu7.bookstore.entity.Review;
import ru.bmstu.iu7.bookstore.entity.User;
import ru.bmstu.iu7.bookstore.repository.BookRepository;
import ru.bmstu.iu7.bookstore.repository.ReviewRepository;
import ru.bmstu.iu7.bookstore.repository.UserRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollaborativeFilteringService — unit-тесты")
class CollaborativeFilteringServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CollaborativeFilteringService service;

    private User user1;
    private User user2;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUserId(1L);
        user1.setUsername("user1");

        user2 = new User();
        user2.setUserId(2L);
        user2.setUsername("user2");

        book1 = new Book();
        book1.setBookId(1L);
        book1.setTitle("Война и мир");
        book1.setPrice(BigDecimal.valueOf(599.0));
        book1.setAvgRating(4.8);
        book1.setRatingCount(120);
        book1.setAuthors(new HashSet<>());
        book1.setCategories(new HashSet<>());

        book2 = new Book();
        book2.setBookId(2L);
        book2.setTitle("Мастер и Маргарита");
        book2.setPrice(BigDecimal.valueOf(499.0));
        book2.setAvgRating(4.9);
        book2.setRatingCount(200);
        book2.setAuthors(new HashSet<>());
        book2.setCategories(new HashSet<>());
    }

    private Review buildReview(User user, Book book, int rating) {
        Review r = new Review();
        r.setUser(user);
        r.setBook(book);
        r.setRating(rating);
        return r;
    }

    @Test
    @DisplayName("КЭ1: пользователь без отзывов — список пуст")
    void testGetRecommendations_UserWithNoReviews_ReturnsEmpty() {
        when(reviewRepository.findByUserUserId(1L)).thenReturn(Collections.emptyList());

        List<BookRecommendation> result = service.getUserBasedRecommendations(1L, 10);

        assertTrue(result.isEmpty());
        verify(reviewRepository).findByUserUserId(1L);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("КЭ2: нет похожих пользователей — список пуст")
    void testGetRecommendations_NoSimilarUsers_ReturnsEmpty() {
        Review review = buildReview(user1, book1, 5);
        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of(review));
        when(userRepository.findAll()).thenReturn(List.of(user1));

        List<BookRecommendation> result = service.getUserBasedRecommendations(1L, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("КЭ3: есть похожий пользователь — рекомендации сформированы")
    void testGetRecommendations_WithSimilarUser_ReturnsRecommendations() {
        Review r1a = buildReview(user1, book1, 5);
        Review r2a = buildReview(user2, book1, 5);
        Review r2b = buildReview(user2, book2, 5);

        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of(r1a));
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(reviewRepository.findByUserUserId(2L)).thenReturn(List.of(r2a, r2b));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(bookRepository.findByIdWithDetails(2L)).thenReturn(book2);

        List<BookRecommendation> result = service.getUserBasedRecommendations(1L, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(reviewRepository, atLeastOnce()).findByUserUserId(anyLong());
    }

    @Test
    @DisplayName("КЭ4: несуществующий userId — список пуст")
    void testGetRecommendations_NonExistentUser_ReturnsEmpty() {
        when(reviewRepository.findByUserUserId(9999L)).thenReturn(Collections.emptyList());

        List<BookRecommendation> result = service.getUserBasedRecommendations(9999L, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Граничный: лимит 0 — список пуст")
    void testGetRecommendations_LimitZero_ReturnsEmpty() {
        Review r = buildReview(user1, book1, 5);
        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of(r));
        when(userRepository.findAll()).thenReturn(List.of(user1));

        List<BookRecommendation> result = service.getUserBasedRecommendations(1L, 0);

        assertTrue(result.isEmpty());
    }
}
