package ru.bookstore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.domain.User;
import ru.bookstore.dto.ActionResponseDto;
import ru.bookstore.dto.RecommendationDto;
import ru.bookstore.service.AuthSessionService;
import ru.bookstore.service.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Slf4j
public class RecommendationController {

    private static final String TOKEN_HEADER = "X-Auth-Token";

    private final RecommendationService recommendationService;
    private final AuthSessionService authSessionService;

    public RecommendationController(RecommendationService recommendationService, AuthSessionService authSessionService) {
        this.recommendationService = recommendationService;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    public List<RecommendationDto> recommendations(
            @RequestHeader(value = TOKEN_HEADER, required = false) String token,
            @RequestParam(defaultValue = "5") int count) {
        int safeCount = Math.max(1, Math.min(count, 20));
        if (token == null || token.isBlank()) {
            log.info("HTTP популярные рекомендации для гостя: n={}", safeCount);
            return recommendationService.getPopularRecommendations(safeCount).stream()
                    .map(this::toRecommendationDto)
                    .toList();
        }

        User user = authSessionService.requireUser(token);
        log.info("HTTP рекомендации: userId={}, n={}", user.getUserId(), safeCount);
        List<Recommendation> recommendations =
                recommendationService.getHybridRecommendations(user.getUserId(), safeCount);
        if (recommendations == null || recommendations.isEmpty()) {
            recommendations = recommendationService.getPopularRecommendations(safeCount);
        }
        return recommendations.stream().map(this::toRecommendationDto).toList();
    }

    @PostMapping("/cache/clear")
    public ActionResponseDto clearCache(@RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        User user = authSessionService.requireUser(token);
        requireAdmin(user);
        log.info("HTTP рекомендации: очистка кэша userId={}", user.getUserId());
        recommendationService.clearRecommendationCaches();
        return new ActionResponseDto(true, "Кэш рекомендаций очищен");
    }

    private RecommendationDto toRecommendationDto(Recommendation recommendation) {
        return new RecommendationDto(
                recommendation.getBook() != null ? recommendation.getBook().getIsbn() : null,
                recommendation.getBook() != null ? recommendation.getBook().getTitle() : null,
                recommendation.getScore(),
                recommendation.getReason(),
                recommendation.getType() != null ? recommendation.getType().name() : null
        );
    }

    private void requireAdmin(User user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав администратора.");
        }
    }
}
