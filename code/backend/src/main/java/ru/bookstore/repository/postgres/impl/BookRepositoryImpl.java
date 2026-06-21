package ru.bookstore.repository.postgres.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Book;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.postgres.jpa.BookJpaRepository;
import ru.bookstore.repository.postgres.mapper.BookEntityMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Profile("postgres")
public class BookRepositoryImpl implements BookRepository {

    private final BookJpaRepository jpaRepo;
    private final BookEntityMapper mapper;

    @Override
    public Optional<Book> findById(String isbn) {
        return jpaRepo.findById(isbn).map(mapper::toDomain);
    }

    @Override
    public Book findByIdWithDetails(String isbn) {
        return mapper.toDomain(jpaRepo.findByIdWithDetails(isbn));
    }

    @Override
    public List<Book> findByCategoryId(Long categoryId) {
        return jpaRepo.findByCategoryId(categoryId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findTopRatedBooks(Double minRating, int limit, int offset) {
        int pageSize = Math.max(limit, 1);
        int page = Math.max(offset, 0) / pageSize;
        return jpaRepo.findTopRatedBooks(minRating, PageRequest.of(page, pageSize)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchBooks(String query, int limit, int offset) {
        int pageSize = Math.max(limit, 1);
        int page = Math.max(offset, 0) / pageSize;
        return jpaRepo.searchBooks(query, PageRequest.of(page, pageSize)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findAll() {
        return jpaRepo.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Book save(Book book) {
        return mapper.toDomain(jpaRepo.save(mapper.toEntity(book)));
    }

    @Override
    public void deleteById(String id) {
        jpaRepo.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepo.existsById(id);
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }
}
