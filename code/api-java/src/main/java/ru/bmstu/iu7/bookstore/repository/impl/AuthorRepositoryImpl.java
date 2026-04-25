package ru.bmstu.iu7.bookstore.repository.impl;

import org.springframework.stereotype.Repository;
import ru.bmstu.iu7.bookstore.entity.Author;
import ru.bmstu.iu7.bookstore.repository.AuthorRepository;
import ru.bmstu.iu7.bookstore.repository.jpa.AuthorJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class AuthorRepositoryImpl implements AuthorRepository {

    private final AuthorJpaRepository jpaRepo;

    public AuthorRepositoryImpl(AuthorJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<Author> findAll() {
        return jpaRepo.findAll();
    }

    @Override
    public Optional<Author> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public Author save(Author author) {
        return jpaRepo.save(author);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepo.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }
}
