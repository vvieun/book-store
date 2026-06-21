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
import ru.bookstore.domain.User;
import ru.bookstore.domain.Wishlist;
import ru.bookstore.dto.ActionResponseDto;
import ru.bookstore.dto.WishlistItemDto;
import ru.bookstore.dto.WishlistUpdateRequestDto;
import ru.bookstore.service.AuthSessionService;
import ru.bookstore.service.WishlistService;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@Slf4j
public class WishlistController {

    private static final String TOKEN_HEADER = "X-Auth-Token";

    private final WishlistService wishlistService;
    private final AuthSessionService authSessionService;

    public WishlistController(WishlistService wishlistService, AuthSessionService authSessionService) {
        this.wishlistService = wishlistService;
        this.authSessionService = authSessionService;
    }

    @GetMapping
    public List<WishlistItemDto> getWishlist(@RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        User user = authSessionService.requireUser(token);
        log.info("HTTP вишлист: userId={}", user.getUserId());
        return wishlistService.getWishlist(user.getUserId()).stream().map(this::toDto).toList();
    }

    @PostMapping
    public WishlistItemDto add(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                               @RequestBody WishlistUpdateRequestDto request) {
        User user = authSessionService.requireUser(token);
        if (request == null || request.getIsbn() == null || request.getIsbn().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isbn обязателен");
        }
        Wishlist saved = wishlistService.addToWishlist(user.getUserId(), request.getIsbn().trim());
        return toDto(saved);
    }

    @DeleteMapping("/{isbn}")
    public ActionResponseDto remove(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                    @PathVariable String isbn) {
        User user = authSessionService.requireUser(token);
        if (isbn == null || isbn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isbn обязателен");
        }
        boolean deleted = wishlistService.removeFromWishlist(user.getUserId(), isbn.trim());
        return new ActionResponseDto(deleted, deleted ? "Удалено" : "Не найдено");
    }

    private WishlistItemDto toDto(Wishlist w) {
        String isbn = w.getBook() != null ? w.getBook().getIsbn() : null;
        String title = w.getBook() != null ? w.getBook().getTitle() : null;
        return new WishlistItemDto(isbn, title);
    }
}

