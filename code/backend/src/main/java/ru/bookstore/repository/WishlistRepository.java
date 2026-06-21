package ru.bookstore.repository;

import ru.bookstore.domain.Wishlist;

import java.util.List;

public interface WishlistRepository {
    List<Wishlist> findByUserUserId(Long userId);

    boolean existsByUserIdAndBookIsbn(Long userId, String isbn);

    Wishlist save(Wishlist wishlist);

    boolean deleteByUserIdAndBookIsbn(Long userId, String isbn);
}
