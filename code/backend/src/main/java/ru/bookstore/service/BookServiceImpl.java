package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.domain.Book;
import ru.bookstore.repository.BookRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findById(String isbn) {
        log.debug("Finding book by isbn: {}", isbn);
        return Optional.ofNullable(bookRepository.findByIdWithDetails(isbn));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        log.debug("Getting all books");
        return bookRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByCategory(Long categoryId) {
        log.debug("Finding books by category: {}", categoryId);
        return bookRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getTopRated(int count) {
        log.debug("Getting top {} rated books", count);
        return bookRepository.findTopRatedBooks(0.0, count, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getTopRated(int count, int offset) {
        log.debug("Getting top {} rated books from offset {}", count, offset);
        return bookRepository.findTopRatedBooks(0.0, count, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query, int limit) {
        log.debug("Searching books with query: {}", query);
        return bookRepository.searchBooks(query, limit, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query, int limit, int offset) {
        log.debug("Searching books with query: {}, offset: {}", query, offset);
        return bookRepository.searchBooks(query, limit, offset);
    }
}
