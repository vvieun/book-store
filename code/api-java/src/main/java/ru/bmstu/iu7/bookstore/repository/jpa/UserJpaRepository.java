package ru.bmstu.iu7.bookstore.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bmstu.iu7.bookstore.entity.User;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
