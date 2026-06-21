package ru.bookstore.repository.postgres.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.User;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.repository.postgres.jpa.UserJpaRepository;
import ru.bookstore.repository.postgres.mapper.UserEntityMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile("postgres")
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepo;
    private final UserEntityMapper mapper;

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepo.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepo.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepo.existsByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return jpaRepo.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepo.save(mapper.toEntity(user)));
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepo.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }

    @Override
    public long countByRole(String role) {
        if (role == null || role.isBlank()) {
            return 0;
        }
        return jpaRepo.countByRoleIgnoreCase(role.trim());
    }
}
