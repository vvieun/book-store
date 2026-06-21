package ru.bookstore.repository.postgres.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Review;
import ru.bookstore.repository.ReviewRepository;
import ru.bookstore.repository.postgres.jpa.ReviewJpaRepository;
import ru.bookstore.repository.postgres.mapper.ReviewEntityMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile("postgres")
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository jpaRepo;
    private final ReviewEntityMapper mapper;

    @Override
    public List<Review> findByUserUserId(Long userId) {
        return jpaRepo.findByUserUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> findByBookIsbn(String isbn) {
        return jpaRepo.findByBookIsbn(isbn).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Long countByBookIsbn(String isbn) {
        return jpaRepo.countByBookIsbn(isbn);
    }

    @Override
    public Double getAverageRatingByBookIsbn(String isbn) {
        return jpaRepo.getAverageRatingByBookIsbn(isbn);
    }

    @Override
    public Review findByUserIdAndBookIsbn(Long userId, String isbn) {
        return mapper.toDomain(jpaRepo.findByUserIdAndBookIsbn(userId, isbn));
    }

    @Override
    public Optional<Review> findById(Long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Review> findAll() {
        return jpaRepo.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Review save(Review review) {
        return mapper.toDomain(jpaRepo.save(mapper.toEntity(review)));
    }

    @Override
    public void delete(Review review) {
        jpaRepo.delete(mapper.toEntity(review));
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }
}
