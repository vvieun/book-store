package ru.bmstu.iu7.bookstore.repository.impl;

import org.springframework.stereotype.Repository;
import ru.bmstu.iu7.bookstore.entity.Review;
import ru.bmstu.iu7.bookstore.repository.ReviewRepository;
import ru.bmstu.iu7.bookstore.repository.jpa.ReviewJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository jpaRepo;

    public ReviewRepositoryImpl(ReviewJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<Review> findByUserUserId(Long userId) {
        return jpaRepo.findByUserUserId(userId);
    }

    @Override
    public List<Review> findByBookBookId(Long bookId) {
        return jpaRepo.findByBookBookId(bookId);
    }

    @Override
    public Long countByBookBookId(Long bookId) {
        return jpaRepo.countByBookBookId(bookId);
    }

    @Override
    public Double getAverageRatingByBookId(Long bookId) {
        return jpaRepo.getAverageRatingByBookId(bookId);
    }

    @Override
    public Review findByUserIdAndBookId(Long userId, Long bookId) {
        return jpaRepo.findByUserIdAndBookId(userId, bookId);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<Review> findAll() {
        return jpaRepo.findAll();
    }

    @Override
    public Review save(Review review) {
        return jpaRepo.save(review);
    }

    @Override
    public void delete(Review review) {
        jpaRepo.delete(review);
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }
}
