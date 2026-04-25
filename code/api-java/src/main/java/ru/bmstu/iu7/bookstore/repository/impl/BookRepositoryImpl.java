package ru.bmstu.iu7.bookstore.repository.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.bmstu.iu7.bookstore.entity.Book;
import ru.bmstu.iu7.bookstore.repository.BookRepository;
import ru.bmstu.iu7.bookstore.repository.jpa.BookJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final BookJpaRepository jpaRepo;

    public BookRepositoryImpl(BookJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<Book> findById(Long bookId) {
        return jpaRepo.findById(bookId);
    }

    @Override
    public Book findByIdWithDetails(Long bookId) {
        return jpaRepo.findByIdWithDetails(bookId);
    }

    @Override
    public List<Book> findByCategoryId(Long categoryId) {
        return jpaRepo.findByCategoryId(categoryId);
    }

    @Override
    public List<Book> findTopRatedBooks(Double minRating, Pageable pageable) {
        return jpaRepo.findTopRatedBooks(minRating, pageable);
    }

    @Override
    public List<Book> searchBooks(String query, Pageable pageable) {
        return jpaRepo.searchBooks(query, pageable);
    }

    @Override
    public List<Book> findAll() {
        return jpaRepo.findAll();
    }

    @Override
    public Book save(Book book) {
        return jpaRepo.save(book);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepo.existsById(id);
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }
}
