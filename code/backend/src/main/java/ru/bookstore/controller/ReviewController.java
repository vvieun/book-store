package ru.bookstore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Review;
import ru.bookstore.domain.User;
import ru.bookstore.dto.ActionResponseDto;
import ru.bookstore.dto.CreateReviewRequestDto;
import ru.bookstore.dto.ReviewDto;
import ru.bookstore.service.AuthSessionService;
import ru.bookstore.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Slf4j
public class ReviewController {

    private static final String TOKEN_HEADER = "X-Auth-Token";

    private final ReviewService reviewService;
    private final AuthSessionService authSessionService;

    public ReviewController(ReviewService reviewService, AuthSessionService authSessionService) {
        this.reviewService = reviewService;
        this.authSessionService = authSessionService;
    }

    @PostMapping
    public ReviewDto saveReview(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                @RequestBody CreateReviewRequestDto request) {
        User user = authSessionService.requireUser(token);
        log.info("HTTP отзыв: создать userId={}, isbn={}, rating={}",
                user.getUserId(),
                request != null ? request.getIsbn() : null,
                request != null ? request.getRating() : null);
        if (request == null || request.getIsbn() == null || request.getIsbn().isBlank() || request.getRating() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите книгу и оценку.");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Оценка должна быть от 1 до 5.");
        }

        Review review = new Review();
        review.setUser(user);
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        review.setBook(book);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return toReviewDto(reviewService.createReview(review));
    }

    @GetMapping
    public List<ReviewDto> allReviews(@RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        User user = authSessionService.requireUser(token);
        requireModerator(user);
        log.info("HTTP отзывы: все (модер.) userId={}", user.getUserId());
        return reviewService.getAllReviews().stream().map(this::toReviewDto).toList();
    }

    @DeleteMapping("/{reviewId}")
    public ActionResponseDto deleteReview(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                          @PathVariable Long reviewId) {
        User user = authSessionService.requireUser(token);
        requireModerator(user);
        log.info("HTTP отзыв: удалить userId={}, reviewId={}", user.getUserId(), reviewId);
        boolean deleted = reviewService.deleteReview(reviewId);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Отзыв не найден.");
        }
        return new ActionResponseDto(true, "Отзыв удалён");
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

    private void requireModerator(User user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if (!"MODERATOR".equals(role) && !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав для модерации.");
        }
    }
}
