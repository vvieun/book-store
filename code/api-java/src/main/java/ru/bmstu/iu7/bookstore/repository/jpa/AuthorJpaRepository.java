package ru.bmstu.iu7.bookstore.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bmstu.iu7.bookstore.entity.Author;

public interface AuthorJpaRepository extends JpaRepository<Author, Long> {
}
