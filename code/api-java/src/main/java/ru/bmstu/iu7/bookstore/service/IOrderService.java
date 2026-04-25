package ru.bmstu.iu7.bookstore.service;

import ru.bmstu.iu7.bookstore.domain.Order;
import ru.bmstu.iu7.bookstore.dto.CreateOrderRequest;

import java.util.List;
import java.util.Optional;

public interface IOrderService {

    Order create(CreateOrderRequest request);

    List<Order> getByUser(Long userId);

    List<Order> getByStatus(String status);

    Optional<Order> getById(Long orderId);
}
