package ru.bookstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Review;
import ru.bookstore.domain.User;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.RecommendationCacheRepository;
import ru.bookstore.repository.ReviewRepository;
import ru.bookstore.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private RecommendationCacheRepository recommendationCacheRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");

        testBook = new Book();
        testBook.setIsbn("isbn-1");
        testBook.setTitle("Test Book");
    }

    private Review buildReview(Long id, Integer rating, String comment) {
        Review r = new Review();
        r.setReviewId(id);
        r.setUser(testUser);
        r.setBook(testBook);
        r.setRating(rating);
        r.setComment(comment);
        return r;
    }

    @Test
    void testCreateReview_NewReview_IsSaved() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById("isbn-1")).thenReturn(Optional.of(testBook));
        when(reviewRepository.findByUserIdAndBookIsbn(1L, "isbn-1")).thenReturn(null);

        Review saved = buildReview(1L, 5, "Отличная книга!");
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);
        when(bookRepository.findById("isbn-1")).thenReturn(Optional.of(testBook));
        when(reviewRepository.getAverageRatingByBookIsbn("isbn-1")).thenReturn(5.0);
        when(reviewRepository.countByBookIsbn("isbn-1")).thenReturn(1L);

        Review result = reviewService.createReview(buildReview(null, 5, "Отличная книга!"));

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Отличная книга!", result.getComment());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void testCreateReview_ExistingReview_IsUpdated() {
        Review existing = buildReview(1L, 3, "Неплохо");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById("isbn-1")).thenReturn(Optional.of(testBook));
        when(reviewRepository.findByUserIdAndBookIsbn(1L, "isbn-1")).thenReturn(existing);
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Review result = reviewService.createReview(buildReview(null, 5, "Изменил мнение!"));

        assertEquals(5, result.getRating());
        assertEquals("Изменил мнение!", result.getComment());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void testCreateReview_UserNotFound_ThrowsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Review review = buildReview(null, 5, "Test");
        review.getUser().setUserId(99L);
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(review));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_BookNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById("isbn-99")).thenReturn(Optional.empty());

        Review review = buildReview(null, 5, "Test");
        review.getBook().setIsbn("isbn-99");
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(review));
        verify(reviewRepository, never()).save(any());
    }

    @Test
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
    void testGetUserReviews_Empty() {
        when(reviewRepository.findByUserUserId(1L)).thenReturn(List.of());

        List<Review> result = reviewService.getUserReviews(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetBookReviews_ReturnsList() {
        when(reviewRepository.findByBookIsbn("isbn-1")).thenReturn(List.of(
                buildReview(1L, 5, "x"),
                buildReview(2L, 4, "y")
        ));

        List<Review> result = reviewService.getBookReviews("isbn-1");

        assertEquals(2, result.size());
        verify(reviewRepository).findByBookIsbn("isbn-1");
    }

    @Test
    void testGetBookReviews_Empty() {
        when(reviewRepository.findByBookIsbn("isbn-1")).thenReturn(List.of());

        List<Review> result = reviewService.getBookReviews("isbn-1");

        assertTrue(result.isEmpty());
    }
}
