package ru.bmstu.iu7.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bmstu.iu7.bookstore.dto.BookRecommendation;
import ru.bmstu.iu7.bookstore.service.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Book recommendation endpoints")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping("/popular")
    @Operation(summary = "Топ популярных книг (доступно без авторизации)")
    public ResponseEntity<List<BookRecommendation>> getPopular(
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(recommendationService.getPopularRecommendations(count));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Персональные рекомендации для авторизованного пользователя")
    public ResponseEntity<List<BookRecommendation>> getUserRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(
                recommendationService.getHybridRecommendations(userId, count));
    }
    
    @PostMapping("/invalidate-cache")
    @Operation(summary = "Сбросить кэш рекомендаций")
    public ResponseEntity<Void> invalidateCache() {
        recommendationService.invalidateCache();
        return ResponseEntity.ok().build();
    }
}
