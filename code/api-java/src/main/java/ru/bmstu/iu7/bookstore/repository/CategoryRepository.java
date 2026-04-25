package ru.bmstu.iu7.bookstore.repository;

import ru.bmstu.iu7.bookstore.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findAll();
    Optional<Category> findById(Long id);
    Category save(Category category);
    boolean existsById(Long id);
    void deleteById(Long id);
    long count();
}
