package ru.bookstore.repository;

import ru.bookstore.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Optional<Book> findById(String isbn);

    Book findByIdWithDetails(String isbn);

    List<Book> findByCategoryId(Long categoryId);

    List<Book> findTopRatedBooks(Double minRating, int limit, int offset);

    List<Book> searchBooks(String query, int limit, int offset);

    List<Book> findAll();

    Book save(Book book);

    void deleteById(String id);

    boolean existsById(String id);

    long count();
}
