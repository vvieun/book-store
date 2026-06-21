package ru.bookstore.repository.mongo.spring_data;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.bookstore.repository.mongo.model.ReviewDoc;

import java.util.List;
import java.util.Optional;

public interface ReviewMongoRepository extends MongoRepository<ReviewDoc, Long> {
    List<ReviewDoc> findByUserId(Long userId);
    List<ReviewDoc> findByBookIsbn(String isbn);
    Optional<ReviewDoc> findByUserIdAndBookIsbn(Long userId, String isbn);
    long countByBookIsbn(String isbn);
}

