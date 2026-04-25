package ru.bmstu.iu7.bookstore.repository.impl;

import org.springframework.stereotype.Repository;
import ru.bmstu.iu7.bookstore.entity.Order;
import ru.bmstu.iu7.bookstore.repository.OrderRepository;
import ru.bmstu.iu7.bookstore.repository.jpa.OrderJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepo;

    public OrderRepositoryImpl(OrderJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId) {
        return jpaRepo.findByUserUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Order> findByStatus(String status) {
        return jpaRepo.findByStatus(status);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepo.findAll();
    }

    @Override
    public Order save(Order order) {
        return jpaRepo.save(order);
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }
}
