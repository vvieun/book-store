package ru.bmstu.iu7.bookstore.repository.impl;

import org.springframework.stereotype.Repository;
import ru.bmstu.iu7.bookstore.entity.Category;
import ru.bmstu.iu7.bookstore.repository.CategoryRepository;
import ru.bmstu.iu7.bookstore.repository.jpa.CategoryJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository jpaRepo;

    public CategoryRepositoryImpl(CategoryJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<Category> findAll() {
        return jpaRepo.findAll();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public Category save(Category category) {
        return jpaRepo.save(category);
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
