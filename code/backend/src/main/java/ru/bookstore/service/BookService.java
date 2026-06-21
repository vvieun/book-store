package ru.bookstore.service;

import ru.bookstore.domain.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {

    Optional<Book> findById(String isbn);

    List<Book> getAllBooks();

    List<Book> findByCategory(Long categoryId);

    List<Book> getTopRated(int count);

    List<Book> getTopRated(int count, int offset);

    List<Book> searchBooks(String query, int limit);

    List<Book> searchBooks(String query, int limit, int offset);
}
