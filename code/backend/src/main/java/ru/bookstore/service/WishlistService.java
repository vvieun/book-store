package ru.bookstore.service;

import ru.bookstore.domain.Wishlist;

import java.util.List;

public interface WishlistService {
    List<Wishlist> getWishlist(Long userId);
    Wishlist addToWishlist(Long userId, String isbn);
    boolean removeFromWishlist(Long userId, String isbn);
}
