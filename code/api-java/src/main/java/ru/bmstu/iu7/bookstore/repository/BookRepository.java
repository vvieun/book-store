package ru.bmstu.iu7.bookstore.repository;

import org.springframework.data.domain.Pageable;
import ru.bmstu.iu7.bookstore.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Optional<Book> findById(Long bookId);

    Book findByIdWithDetails(Long bookId);

    List<Book> findByCategoryId(Long categoryId);

    List<Book> findTopRatedBooks(Double minRating, Pageable pageable);

    List<Book> searchBooks(String query, Pageable pageable);

    List<Book> findAll();

    Book save(Book book);

    void deleteById(Long id);

    boolean existsById(Long id);

    long count();
}
