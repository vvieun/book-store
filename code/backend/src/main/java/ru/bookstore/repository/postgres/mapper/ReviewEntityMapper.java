package ru.bookstore.repository.postgres.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.bookstore.domain.Review;

@Component
@RequiredArgsConstructor
public class ReviewEntityMapper {

    private final UserEntityMapper userMapper;
    private final BookEntityMapper bookMapper;

    public Review toDomain(ru.bookstore.repository.postgres.model.Review entity) {
        if (entity == null) {
            return null;
        }
        return new Review(
                entity.getReviewId(),
                userMapper.toDomain(entity.getUser()),
                bookMapper.toDomain(entity.getBook()),
                entity.getRating(),
                entity.getComment(),
                entity.getCreatedAt()
        );
    }

    public ru.bookstore.repository.postgres.model.Review toEntity(Review domain) {
        if (domain == null) {
            return null;
        }
        ru.bookstore.repository.postgres.model.Review review = new ru.bookstore.repository.postgres.model.Review();
        review.setReviewId(domain.getReviewId());
        review.setUser(userMapper.toEntity(domain.getUser()));
        review.setBook(bookMapper.toEntity(domain.getBook()));
        review.setRating(domain.getRating());
        review.setComment(domain.getComment());
        review.setCreatedAt(domain.getCreatedAt());
        return review;
    }
}
