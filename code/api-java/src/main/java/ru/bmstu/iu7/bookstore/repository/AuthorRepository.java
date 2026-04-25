package ru.bmstu.iu7.bookstore.repository;

import ru.bmstu.iu7.bookstore.entity.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository {
    List<Author> findAll();
    Optional<Author> findById(Long id);
    Author save(Author author);
    boolean existsById(Long id);
    void deleteById(Long id);
    long count();
}
