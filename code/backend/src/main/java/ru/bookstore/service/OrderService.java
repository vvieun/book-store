package ru.bookstore.service;

import ru.bookstore.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    Order create(Order order);

    List<Order> getByUser(Long userId);

    List<Order> getByStatus(String status);

    Optional<Order> getById(Long orderId);

    List<Order> listAllOrdersForStaff();
    
    Order updateOrderStatus(Long orderId, String newStatus);
}
