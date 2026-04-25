package ru.bmstu.iu7.bookstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bmstu.iu7.bookstore.domain.Review;
import ru.bmstu.iu7.bookstore.entity.Book;
import ru.bmstu.iu7.bookstore.entity.User;
import ru.bmstu.iu7.bookstore.repository.BookRepository;
import ru.bmstu.iu7.bookstore.repository.ReviewRepository;
import ru.bmstu.iu7.bookstore.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService — unit-тесты")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");

        testBook = new Book();
        testBook.setBookId(1L);
        testBook.setTitle("Test Book");
    }

    private ru.bmstu.iu7.bookstore.entity.Review buildReview(Long id, Integer rating, String comment) {
        ru.bmstu.iu7.bookstore.entity.Review r = new ru.bmstu.iu7.bookstore.entity.Review();
        r.setReviewId(id);
        r.setUser(testUser);
        r.setBook(testBook);
        r.setRating(rating);
        r.setComment(comment);
        return r;
    }

    @Test
    @DisplayName("КЭ1: создание нового отзыва — сохраняется в БД")
    void testCreateReview_NewReview_IsSaved() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reviewRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(null);

        ru.bmstu.iu7.bookstore.entity.Review saved = buildReview(1L, 5, "Отличная книга!");
        when(reviewRepository.save(any(ru.bmstu.iu7.bookstore.entity.Review.class))).thenReturn(saved);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reviewRepository.getAverageRatingByBookId(1L)).thenReturn(5.0);
        when(reviewRepository.countByBookBookId(1L)).thenReturn(1L);

        Review result = reviewService.createReview(1L, 1L, 5, "Отличная книга!");

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Отличная книга!", result.getComment());
        verify(reviewRepository).save(any(ru.bmstu.iu7.bookstore.entity.Review.class));
    }

    @Test
    @DisplayName("КЭ2: обновление существующего отзыва — рейтинг меняется")
    void testCreateReview_ExistingReview_IsUpdated() {
        ru.bmstu.iu7.bookstore.entity.Review existing = buildReview(1L, 3, "Неплохо");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reviewRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(existing);
        when(reviewRepository.save(existing)).thenReturn(existing);

        Review result = reviewService.createReview(1L, 1L, 5, "Изменил мнение!");

        assertEquals(5, result.getRating());
        assertEquals("Изменил мнение!", result.getComment());
        verify(reviewRepository).save(existing);
    }

    @Test
    @DisplayName("КЭ3: пользователь не найден — IllegalArgumentException")
    void testCreateReview_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(99L, 1L, 5, "Test"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("КЭ4: книга не найдена — IllegalArgumentException")
    void testCreateReview_BookNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(1L, 99L, 5, "Test"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserReviews — возвращает список отзывов пользователя")
    void testGetUserReviews_ReturnsList() {
        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of(
                buildReview(1L, 5, "a"),
                buildReview(2L, 4, "b"),
                buildReview(3L, 3, "c")
        ));

        List<Review> result = reviewService.getUserReviews(1L);

        assertEquals(3, result.size());
        verify(reviewRepository).findByUserUserId(1L);
    }

    @Test
    @DisplayName("getUserReviews — пользователь без отзывов")
    void testGetUserReviews_Empty() {
        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of());

        List<Review> result = reviewService.getUserReviews(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getBookReviews — возвращает список отзывов книги")
    void testGetBookReviews_ReturnsList() {
        when(reviewRepository.findByBookBookId(1L)).thenReturn(List.of(
                buildReview(1L, 5, "x"),
                buildReview(2L, 4, "y")
        ));

        List<Review> result = reviewService.getBookReviews(1L);

        assertEquals(2, result.size());
        verify(reviewRepository).findByBookBookId(1L);
    }

    @Test
    @DisplayName("getBookReviews — книга без отзывов")
    void testGetBookReviews_Empty() {
        when(reviewRepository.findByBookBookId(1L)).thenReturn(List.of());

        List<Review> result = reviewService.getBookReviews(1L);

        assertTrue(result.isEmpty());
    }
}
