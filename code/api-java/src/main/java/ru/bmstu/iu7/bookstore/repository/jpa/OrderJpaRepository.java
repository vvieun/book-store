package ru.bmstu.iu7.bookstore.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bmstu.iu7.bookstore.entity.Order;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStatus(String status);
}
