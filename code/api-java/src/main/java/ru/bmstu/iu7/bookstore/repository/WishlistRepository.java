package ru.bmstu.iu7.bookstore.repository;

import ru.bmstu.iu7.bookstore.entity.Wishlist;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository {

    List<Wishlist> findByUserUserId(Long userId);

    boolean existsByUserUserIdAndBookBookId(Long userId, Long bookId);

    void deleteByUserUserIdAndBookBookId(Long userId, Long bookId);

    Optional<Wishlist> findById(Long id);

    Wishlist save(Wishlist wishlist);
}
