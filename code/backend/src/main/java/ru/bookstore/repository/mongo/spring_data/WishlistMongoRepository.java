package ru.bookstore.repository.mongo.spring_data;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.bookstore.repository.mongo.model.WishlistDoc;

import java.util.List;

public interface WishlistMongoRepository extends MongoRepository<WishlistDoc, Long> {
    List<WishlistDoc> findByUserId(Long userId);
    boolean existsByUserIdAndBookIsbn(Long userId, String isbn);
    void deleteByUserIdAndBookIsbn(Long userId, String isbn);
}

