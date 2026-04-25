package ru.bmstu.iu7.bookstore.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bmstu.iu7.bookstore.entity.Wishlist;

import java.util.List;

public interface WishlistJpaRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUserUserId(Long userId);

    boolean existsByUserUserIdAndBookBookId(Long userId, Long bookId);

    void deleteByUserUserIdAndBookBookId(Long userId, Long bookId);
}
