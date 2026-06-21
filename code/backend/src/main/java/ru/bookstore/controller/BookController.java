package ru.bookstore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.domain.Review;
import ru.bookstore.dto.BookDto;
import ru.bookstore.dto.ReviewDto;
import ru.bookstore.service.AuthSessionService;
import ru.bookstore.service.BookService;
import ru.bookstore.service.RecommendationService;
import ru.bookstore.service.ReviewService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/books")
@Slf4j
public class BookController {

    private static final String TOKEN_HEADER = "X-Auth-Token";

    private final BookService bookService;
    private final ReviewService reviewService;
    private final RecommendationService recommendationService;
    private final AuthSessionService authSessionService;

    public BookController(BookService bookService,
                          ReviewService reviewService,
                          RecommendationService recommendationService,
                          AuthSessionService authSessionService) {
        this.bookService = bookService;
        this.reviewService = reviewService;
        this.recommendationService = recommendationService;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    public List<BookDto> books(@org.springframework.web.bind.annotation.RequestHeader(value = TOKEN_HEADER, required = false) String token,
                               @RequestParam(required = false) String query,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size) {
        log.info("HTTP books list requested: query={}, page={}, size={}", query, page, size);
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 50));
        int offset = safePage * safeSize;
        List<Book> books;
        if (query != null && !query.isBlank()) {
            books = bookService.searchBooks(query.trim(), safeSize, offset);
        } else if (token != null && !token.isBlank()) {
            Long userId = authSessionService.requireUser(token).getUserId();
            books = recommendationService.getHybridRecommendations(userId, offset + safeSize).stream()
                    .map(Recommendation::getBook)
                    .filter(Objects::nonNull)
                    .skip(offset)
                    .limit(safeSize)
                    .toList();
        } else {
            books = bookService.getTopRated(safeSize, offset);
        }
        return books.stream().map(this::toBookDto).toList();
    }

    @GetMapping("/{isbn}")
    public BookDto book(@PathVariable String isbn) {
        log.info("HTTP book details requested: isbn={}", isbn);
        Book book = bookService.findById(isbn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Книга не найдена."));
        return toBookDto(book);
    }

    @GetMapping("/{isbn}/reviews")
    public List<ReviewDto> reviewsByBook(@PathVariable String isbn) {
        log.info("HTTP book reviews requested: isbn={}", isbn);
        return reviewService.getBookReviews(isbn).stream().map(this::toReviewDto).toList();
    }

    private BookDto toBookDto(Book book) {
        return new BookDto(
                book.getIsbn(),
                book.getTitle(),
                book.getDescription(),
                book.getPrice(),
                book.getAvgRating(),
                book.getRatingCount()
        );
    }

    private ReviewDto toReviewDto(Review review) {
        return new ReviewDto(
                review.getReviewId(),
                review.getUser() != null ? review.getUser().getUserId() : null,
                review.getUser() != null ? review.getUser().getUsername() : null,
                review.getBook() != null ? review.getBook().getIsbn() : null,
                review.getRating(),
                review.getComment()
        );
    }
}
