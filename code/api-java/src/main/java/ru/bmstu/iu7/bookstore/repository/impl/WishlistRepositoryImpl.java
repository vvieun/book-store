package ru.bmstu.iu7.bookstore.repository.impl;

import org.springframework.stereotype.Repository;
import ru.bmstu.iu7.bookstore.entity.Wishlist;
import ru.bmstu.iu7.bookstore.repository.WishlistRepository;
import ru.bmstu.iu7.bookstore.repository.jpa.WishlistJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class WishlistRepositoryImpl implements WishlistRepository {

    private final WishlistJpaRepository jpaRepo;

    public WishlistRepositoryImpl(WishlistJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<Wishlist> findByUserUserId(Long userId) {
        return jpaRepo.findByUserUserId(userId);
    }

    @Override
    public boolean existsByUserUserIdAndBookBookId(Long userId, Long bookId) {
        return jpaRepo.existsByUserUserIdAndBookBookId(userId, bookId);
    }

    @Override
    public void deleteByUserUserIdAndBookBookId(Long userId, Long bookId) {
        jpaRepo.deleteByUserUserIdAndBookBookId(userId, bookId);
    }

    @Override
    public Optional<Wishlist> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public Wishlist save(Wishlist wishlist) {
        return jpaRepo.save(wishlist);
    }
}
