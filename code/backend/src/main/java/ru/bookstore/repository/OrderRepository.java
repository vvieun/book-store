package ru.bookstore.repository;

import ru.bookstore.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStatus(String status);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    List<Order> findAllOrderByCreatedAtDescWithDetails();

    Optional<Order> findByIdWithDetails(Long id);

    Order save(Order order);

    long count();
}
