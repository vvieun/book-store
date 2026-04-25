package ru.bmstu.iu7.bookstore.repository;

import ru.bmstu.iu7.bookstore.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStatus(String status);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    Order save(Order order);

    long count();
}
