package ru.bookstore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.RecommendationCacheRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private CollaborativeFilteringService collaborativeService;
    @Mock
    private ContentBasedFilteringService contentBasedService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private RecommendationCacheRepository recommendationCacheRepository;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private Recommendation buildRec(String isbn, double score,
                                    Recommendation.RecommendationType type) {
        Book book = new Book();
        book.setIsbn(isbn);
        return new Recommendation(book, score, "reason", type);
    }

    @Test
    void testGetHybridRecommendations_CacheHit() {
        Recommendation rec = buildRec("isbn-1", 0.9, Recommendation.RecommendationType.CONTENT_BASED);
        when(recommendationCacheRepository.getHybridRecommendations(1L, 10)).thenReturn(List.of(rec));

        List<Recommendation> result = recommendationService.getHybridRecommendations(1L, 10);

        assertEquals(1, result.size());
        assertEquals("isbn-1", result.get(0).getBook().getIsbn());
        verify(recommendationCacheRepository).getHybridRecommendations(1L, 10);
        verifyNoInteractions(collaborativeService, contentBasedService);
    }

    @Test
    void testGetHybridRecommendations_CacheMissCalculatesAndCaches() {
        Recommendation collab = buildRec("isbn-1", 0.8, Recommendation.RecommendationType.COLLABORATIVE);
        Recommendation content = buildRec("isbn-2", 0.7, Recommendation.RecommendationType.CONTENT_BASED);

        when(recommendationCacheRepository.getHybridRecommendations(1L, 10)).thenReturn(null);
        when(collaborativeService.getUserBasedRecommendations(1L, 10)).thenReturn(List.of(collab));
        when(contentBasedService.getRecommendationsByUserPreferences(1L, 10)).thenReturn(List.of(content));
        when(bookRepository.findTopRatedBooks(0.0, 20, 0)).thenReturn(List.of());

        List<Recommendation> result = recommendationService.getHybridRecommendations(1L, 10);

        assertEquals(2, result.size());
        verify(recommendationCacheRepository).putHybridRecommendations(eq(1L), eq(10), anyList());
    }

    @Test
    void testGetHybridRecommendations_FallsBackToPopularWhenNotEnoughPersonalized() {
        Recommendation collab = buildRec("isbn-1", 0.8, Recommendation.RecommendationType.COLLABORATIVE);
        when(recommendationCacheRepository.getHybridRecommendations(1L, 3)).thenReturn(null);
        when(collaborativeService.getUserBasedRecommendations(1L, 3)).thenReturn(List.of(collab));
        when(contentBasedService.getRecommendationsByUserPreferences(1L, 3)).thenReturn(List.of());

        Book popularBook = new Book();
        popularBook.setIsbn("isbn-2");
        popularBook.setAvgRating(4.9);
        when(bookRepository.findTopRatedBooks(0.0, 6, 0)).thenReturn(List.of(popularBook));

        List<Recommendation> result = recommendationService.getHybridRecommendations(1L, 3);

        assertEquals(2, result.size());
        assertEquals("isbn-1", result.get(0).getBook().getIsbn());
        assertEquals("isbn-2", result.get(1).getBook().getIsbn());
    }

    @Test
    void testGetPopularRecommendations_CacheHit() {
        Recommendation rec = buildRec("isbn-1", 4.9, Recommendation.RecommendationType.CONTENT_BASED);
        when(recommendationCacheRepository.getPopularRecommendations(5)).thenReturn(List.of(rec));

        List<Recommendation> result = recommendationService.getPopularRecommendations(5);

        assertEquals(1, result.size());
        verify(recommendationCacheRepository).getPopularRecommendations(5);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void testGetPopularRecommendations_CacheMissCalculatesAndCaches() {
        Book book = new Book();
        book.setIsbn("isbn-1");
        book.setAvgRating(4.7);

        when(recommendationCacheRepository.getPopularRecommendations(5)).thenReturn(null);
        when(bookRepository.findTopRatedBooks(0.0, 5, 0)).thenReturn(List.of(book));

        List<Recommendation> result = recommendationService.getPopularRecommendations(5);

        assertEquals(1, result.size());
        verify(recommendationCacheRepository).putPopularRecommendations(eq(5), anyList());
    }
}
