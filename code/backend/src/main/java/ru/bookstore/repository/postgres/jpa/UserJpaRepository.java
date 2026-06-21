package ru.bookstore.repository.postgres.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bookstore.repository.postgres.model.User;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    long countByRoleIgnoreCase(String role);
}
