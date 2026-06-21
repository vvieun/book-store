package ru.bookstore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Order;
import ru.bookstore.domain.OrderItem;
import ru.bookstore.domain.User;
import ru.bookstore.dto.CreateOrderRequestDto;
import ru.bookstore.dto.OrderDto;
import ru.bookstore.dto.OrderItemDto;
import ru.bookstore.dto.UpdateOrderStatusRequestDto;
import ru.bookstore.service.AuthSessionService;
import ru.bookstore.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private static final String TOKEN_HEADER = "X-Auth-Token";

    private final OrderService orderService;
    private final AuthSessionService authSessionService;

    public OrderController(OrderService orderService, AuthSessionService authSessionService) {
        this.orderService = orderService;
        this.authSessionService = authSessionService;
    }

    @PostMapping
    public OrderDto createOrder(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                @RequestBody CreateOrderRequestDto request) {
        User user = authSessionService.requireUser(token);
        log.info("HTTP заказ: создать userId={}, isbn={}, qty={}",
                user.getUserId(),
                request != null ? request.getIsbn() : null,
                request != null ? request.getQuantity() : null);
        if (request == null || request.getIsbn() == null || request.getIsbn().isBlank()
                || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите книгу и количество больше нуля.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        OrderItem item = new OrderItem();
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        item.setBook(book);
        item.setQuantity(request.getQuantity());
        item.setPrice(BigDecimal.ZERO);
        order.setItems(List.of(item));
        return toOrderDto(orderService.create(order));
    }

    @GetMapping
    public List<OrderDto> listAllOrders(@RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        User user = authSessionService.requireUser(token);
        requireModeratorOrAdmin(user);
        log.info("HTTP заказы: все (персонал) userId={}", user.getUserId());
        return orderService.listAllOrdersForStaff().stream().map(this::toOrderDto).toList();
    }

    @GetMapping("/my")
    public List<OrderDto> myOrders(@RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        User user = authSessionService.requireUser(token);
        log.info("HTTP заказы: мои userId={}", user.getUserId());
        return orderService.getByUser(user.getUserId()).stream().map(this::toOrderDto).toList();
    }

    @PatchMapping("/{orderId}/status")
    public OrderDto patchOrderStatus(
            @RequestHeader(value = TOKEN_HEADER, required = false) String token,
            @PathVariable Long orderId,
            @RequestBody(required = false) UpdateOrderStatusRequestDto request) {
        User user = authSessionService.requireUser(token);
        requireModeratorOrAdmin(user);
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите статус заказа.");
        }
        log.info("HTTP заказ: смена статуса staffUserId={}, orderId={}, status={}",
                user.getUserId(), orderId, request.getStatus());
        Order updated = orderService.updateOrderStatus(orderId, request.getStatus());
        return toOrderDto(updated);
    }

    private OrderDto toOrderDto(Order order) {
        Long buyerUserId = order.getUser() != null ? order.getUser().getUserId() : null;
        String buyerUsername = order.getUser() != null ? order.getUser().getUsername() : null;
        List<OrderItemDto> items = order.getItems() == null ? List.of() : order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getBook() != null ? item.getBook().getIsbn() : null,
                        item.getBook() != null ? item.getBook().getTitle() : null,
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();
        return new OrderDto(
                order.getOrderId(),
                order.getStatus(),
                order.getTotalAmount(),
                buyerUserId,
                buyerUsername,
                order.getCreatedAt(),
                items
        );
    }

    private void requireModeratorOrAdmin(User user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if (!"MODERATOR".equals(role) && !"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав для управления заказами.");
        }
    }
}
