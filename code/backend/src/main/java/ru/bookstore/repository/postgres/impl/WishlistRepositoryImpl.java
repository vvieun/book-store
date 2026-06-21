package ru.bookstore.repository.postgres.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Wishlist;
import ru.bookstore.repository.WishlistRepository;
import ru.bookstore.repository.postgres.jpa.WishlistJpaRepository;
import ru.bookstore.repository.postgres.mapper.WishlistEntityMapper;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile("postgres")
public class WishlistRepositoryImpl implements WishlistRepository {

    private final WishlistJpaRepository jpaRepo;
    private final WishlistEntityMapper mapper;

    @Override
    public List<Wishlist> findByUserUserId(Long userId) {
        return jpaRepo.findByUserUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndBookIsbn(Long userId, String isbn) {
        return jpaRepo.existsByUserUserIdAndBookIsbn(userId, isbn);
    }

    @Override
    public Wishlist save(Wishlist wishlist) {
        return mapper.toDomain(jpaRepo.save(mapper.toEntity(wishlist)));
    }

    @Override
    public boolean deleteByUserIdAndBookIsbn(Long userId, String isbn) {
        if (!jpaRepo.existsByUserUserIdAndBookIsbn(userId, isbn)) {
            return false;
        }
        jpaRepo.deleteByUserUserIdAndBookIsbn(userId, isbn);
        return true;
    }
}
