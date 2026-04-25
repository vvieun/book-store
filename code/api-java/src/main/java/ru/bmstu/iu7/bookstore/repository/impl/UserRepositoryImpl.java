package ru.bmstu.iu7.bookstore.repository.impl;

import org.springframework.stereotype.Repository;
import ru.bmstu.iu7.bookstore.entity.User;
import ru.bmstu.iu7.bookstore.repository.UserRepository;
import ru.bmstu.iu7.bookstore.repository.jpa.UserJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepo;

    public UserRepositoryImpl(UserJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepo.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepo.findByUsername(username);
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
        return jpaRepo.findAll();
    }

    @Override
    public User save(User user) {
        return jpaRepo.save(user);
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
}
