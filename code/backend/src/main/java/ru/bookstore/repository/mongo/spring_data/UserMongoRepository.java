package ru.bookstore.repository.mongo.spring_data;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.bookstore.repository.mongo.model.UserDoc;

import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<UserDoc, Long> {
    Optional<UserDoc> findByEmail(String email);
    Optional<UserDoc> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    long countByRole(String role);
}

