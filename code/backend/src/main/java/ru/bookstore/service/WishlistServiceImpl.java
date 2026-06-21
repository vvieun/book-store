package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.User;
import ru.bookstore.domain.Wishlist;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.repository.WishlistRepository;
import ru.bookstore.service.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Wishlist> getWishlist(Long userId) {
        if (userId == null) {
            throw new ValidationException("userId обязателен");
        }
        return wishlistRepository.findByUserUserId(userId);
    }

    @Override
    @Transactional
    public Wishlist addToWishlist(Long userId, String isbn) {
        if (userId == null) {
            throw new ValidationException("userId обязателен");
        }
        if (isbn == null || isbn.isBlank()) {
            throw new ValidationException("isbn обязателен");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new ValidationException("Книга не найдена: " + isbn));

        if (wishlistRepository.existsByUserIdAndBookIsbn(userId, isbn)) {
            log.info("Вишлист: уже есть userId={}, isbn={}", userId, isbn);
            return new Wishlist(null, user, book, LocalDateTime.now());
        }

        Wishlist wishlist = new Wishlist(null, user, book, LocalDateTime.now());
        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("Вишлист: + userId={}, isbn={}", userId, isbn);
        return saved;
    }

    @Override
    @Transactional
    public boolean removeFromWishlist(Long userId, String isbn) {
        if (userId == null) {
            throw new ValidationException("userId обязателен");
        }
        if (isbn == null || isbn.isBlank()) {
            throw new ValidationException("isbn обязателен");
        }
        boolean deleted = wishlistRepository.deleteByUserIdAndBookIsbn(userId, isbn);
        log.info("Вишлист: - userId={}, isbn={}, ok={}", userId, isbn, deleted);
        return deleted;
    }
}
