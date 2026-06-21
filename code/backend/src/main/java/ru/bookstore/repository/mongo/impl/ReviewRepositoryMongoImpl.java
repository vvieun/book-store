package ru.bookstore.repository.mongo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Review;
import ru.bookstore.repository.ReviewRepository;
import ru.bookstore.repository.mongo.mapper.ReviewMongoMapper;
import ru.bookstore.repository.mongo.model.ReviewDoc;
import ru.bookstore.repository.mongo.service.MongoSequenceService;
import ru.bookstore.repository.mongo.spring_data.ReviewMongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("mongo")
public class ReviewRepositoryMongoImpl implements ReviewRepository {

    private final ReviewMongoRepository mongoRepo;
    private final MongoOperations mongo;
    private final ReviewMongoMapper mapper;
    private final MongoSequenceService seq;

    @Override
    public List<Review> findByUserUserId(Long userId) {
        return mongoRepo.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Review> findByBookIsbn(String isbn) {
        return mongoRepo.findByBookIsbn(isbn).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Long countByBookIsbn(String isbn) {
        return mongoRepo.countByBookIsbn(isbn);
    }

    @Override
    public Double getAverageRatingByBookIsbn(String isbn) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("bookIsbn").is(isbn)),
                Aggregation.group("bookIsbn").avg("rating").as("avg")
        );
        AggregationResults<AvgResult> res = mongo.aggregate(agg, "reviews", AvgResult.class);
        AvgResult r = res.getUniqueMappedResult();
        return r == null ? null : r.getAvg();
    }

    @Override
    public Review findByUserIdAndBookIsbn(Long userId, String isbn) {
        return mongoRepo.findByUserIdAndBookIsbn(userId, isbn).map(mapper::toDomain).orElse(null);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return mongoRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Review> findAll() {
        return mongoRepo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Review save(Review review) {
        ReviewDoc doc = mapper.toDoc(review);
        if (doc.getReviewId() == null) {
            // Если это update существующего отзыва, reviewId уже должен быть.
            doc.setReviewId(seq.next("reviews"));
        }
        if (doc.getCreatedAt() == null) {
            doc.setCreatedAt(LocalDateTime.now());
        }
        return mapper.toDomain(mongoRepo.save(doc));
    }

    @Override
    public void delete(Review review) {
        if (review == null || review.getReviewId() == null) {
            return;
        }
        mongoRepo.deleteById(review.getReviewId());
    }

    @Override
    public long count() {
        return mongoRepo.count();
    }

    private static class AvgResult {
        private Double avg;
        public Double getAvg() { return avg; }
        public void setAvg(Double avg) { this.avg = avg; }
    }
}

