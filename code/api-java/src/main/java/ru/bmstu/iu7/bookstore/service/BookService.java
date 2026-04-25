package ru.bmstu.iu7.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bmstu.iu7.bookstore.domain.Book;
import ru.bmstu.iu7.bookstore.repository.BookRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService implements IBookService {

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findById(Long bookId) {
        log.debug("Finding book by id: {}", bookId);
        return Optional.ofNullable(bookRepository.findByIdWithDetails(bookId))
                .map(DomainMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findByCategory(Long categoryId) {
        log.debug("Finding books by category: {}", categoryId);
        return bookRepository.findByCategoryId(categoryId).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getTopRated(int count) {
        log.debug("Getting top {} rated books", count);
        return bookRepository.findTopRatedBooks(0.0, PageRequest.of(0, count)).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query, int limit) {
        log.debug("Searching books with query: {}", query);
        return bookRepository.searchBooks(query, PageRequest.of(0, limit)).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }
}
