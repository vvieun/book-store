package ru.bookstore.repository.postgres.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bookstore.repository.postgres.model.Wishlist;

import java.util.List;

public interface WishlistJpaRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserUserId(Long userId);

    boolean existsByUserUserIdAndBookIsbn(Long userId, String isbn);

    void deleteByUserUserIdAndBookIsbn(Long userId, String isbn);
}
