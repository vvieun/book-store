package ru.bookstore.repository.mongo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.User;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.repository.mongo.mapper.UserMongoMapper;
import ru.bookstore.repository.mongo.model.UserDoc;
import ru.bookstore.repository.mongo.service.MongoSequenceService;
import ru.bookstore.repository.mongo.spring_data.UserMongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("mongo")
public class UserRepositoryMongoImpl implements UserRepository {

    private final UserMongoRepository mongoRepo;
    private final UserMongoMapper mapper;
    private final MongoSequenceService seq;

    @Override
    public Optional<User> findById(Long id) {
        return mongoRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return mongoRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return mongoRepo.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return mongoRepo.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return mongoRepo.existsByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return mongoRepo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public User save(User user) {
        UserDoc doc = mapper.toDoc(user);
        if (doc.getUserId() == null) {
            doc.setUserId(seq.next("users"));
        }
        if (doc.getCreatedAt() == null) {
            doc.setCreatedAt(LocalDateTime.now());
        }
        try {
            return mapper.toDomain(mongoRepo.save(doc));
        } catch (DuplicateKeyException e) {
            // пробрасываем как runtime: бизнес-уровень уже мапит в ConflictException/ValidationException
            throw e;
        }
    }

    @Override
    public boolean existsById(Long id) {
        return mongoRepo.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        mongoRepo.deleteById(id);
    }

    @Override
    public long count() {
        return mongoRepo.count();
    }

    @Override
    public long countByRole(String role) {
        if (role == null || role.isBlank()) {
            return 0;
        }
        return mongoRepo.countByRole(role.trim());
    }
}

