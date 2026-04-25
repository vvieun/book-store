package ru.bmstu.iu7.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.bmstu.iu7.bookstore.domain.Review;
import ru.bmstu.iu7.bookstore.dto.ReviewRequest;
import ru.bmstu.iu7.bookstore.security.UserDetailsServiceImpl;
import ru.bmstu.iu7.bookstore.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Отзывы на книги")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping
    @Operation(summary = "Создать или обновить отзыв (требуется авторизация)")
    public ResponseEntity<Review> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userDetailsService.loadUserEntityByUsername(principal.getUsername()).getUserId();
        Review review = reviewService.createReview(
                userId,
                request.getBookId(),
                request.getRating(),
                request.getComment()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Отзывы пользователя")
    public ResponseEntity<List<Review>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getUserReviews(userId));
    }

    @GetMapping("/books/{bookId}")
    @Operation(summary = "Отзывы на книгу")
    public ResponseEntity<List<Review>> getBookReviews(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getBookReviews(bookId));
    }
}
