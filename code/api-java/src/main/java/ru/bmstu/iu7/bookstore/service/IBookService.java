package ru.bmstu.iu7.bookstore.service;

import ru.bmstu.iu7.bookstore.domain.Book;

import java.util.List;
import java.util.Optional;

public interface IBookService {

    Optional<Book> findById(Long bookId);

    List<Book> findByCategory(Long categoryId);

    List<Book> getTopRated(int count);

    List<Book> searchBooks(String query, int limit);
}
