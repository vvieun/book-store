package ru.bookstore.repository.postgres.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bookstore.repository.postgres.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStatus(String status);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items items
            LEFT JOIN FETCH items.book
            LEFT JOIN FETCH o.user
            ORDER BY o.createdAt DESC
            """)
    List<Order> findAllOrderByCreatedAtDescWithDetails();

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.items items
            LEFT JOIN FETCH items.book
            LEFT JOIN FETCH o.user
            WHERE o.orderId = :id
            """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}
