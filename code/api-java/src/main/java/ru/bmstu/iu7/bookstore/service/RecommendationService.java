package ru.bmstu.iu7.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bmstu.iu7.bookstore.dto.BookRecommendation;
import ru.bmstu.iu7.bookstore.repository.BookRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService implements IRecommendationService {

    private final ICollaborativeFilteringService collaborativeService;
    private final IContentBasedFilteringService contentBasedService;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BookRecommendation> getHybridRecommendations(Long userId, int count) {
        log.debug("Generating hybrid recommendations for user {}", userId);

        int half = count / 2;
        List<BookRecommendation> collaborative = collaborativeService.getUserBasedRecommendations(userId, half);
        List<BookRecommendation> contentBased = contentBasedService.getRecommendationsByUserPreferences(userId, half);
        return mergeAndRank(collaborative, contentBased, count);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookRecommendation> getPopularRecommendations(int count) {
        log.debug("Generating popular recommendations, count={}", count);
        return bookRepository.findTopRatedBooks(0.0, PageRequest.of(0, count)).stream()
                .map(book -> new BookRecommendation(
                        DomainMapper.toDomain(book),
                        book.getAvgRating() != null ? book.getAvgRating() : 0.0,
                        "Popular book",
                        BookRecommendation.RecommendationType.CONTENT_BASED))
                .collect(Collectors.toList());
    }

    private List<BookRecommendation> mergeAndRank(List<BookRecommendation> list1,
                                                  List<BookRecommendation> list2,
                                                  int maxCount) {
        Map<Long, BookRecommendation> merged = new LinkedHashMap<>();

        for (BookRecommendation rec : list1) {
            merged.put(rec.getBook().getBookId(), rec);
        }

        for (BookRecommendation rec : list2) {
            Long bookId = rec.getBook().getBookId();
            if (merged.containsKey(bookId)) {
                BookRecommendation existing = merged.get(bookId);
                existing.setScore((existing.getScore() + rec.getScore()) / 2.0);
                existing.setType(BookRecommendation.RecommendationType.HYBRID);
                existing.setReason("Based on similar users and your preferences");
            } else {
                merged.put(bookId, rec);
            }
        }

        return merged.values().stream()
                .sorted(Comparator.comparing(BookRecommendation::getScore).reversed())
                .limit(maxCount)
                .collect(Collectors.toList());
    }

    public void invalidateCache() {
        log.debug("No-op cache invalidation in domain-style implementation");
    }
}
