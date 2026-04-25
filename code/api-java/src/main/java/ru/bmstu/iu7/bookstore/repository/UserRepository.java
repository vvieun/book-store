package ru.bmstu.iu7.bookstore.repository;

import ru.bmstu.iu7.bookstore.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findAll();

    User save(User user);

    boolean existsById(Long id);

    void deleteById(Long id);

    long count();
}
