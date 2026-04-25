package ru.bmstu.iu7.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.bmstu.iu7.bookstore.domain.Order;
import ru.bmstu.iu7.bookstore.dto.CreateOrderRequest;
import ru.bmstu.iu7.bookstore.security.UserDetailsServiceImpl;
import ru.bmstu.iu7.bookstore.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API заказов")
public class OrderController {

    private final OrderService orderService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping
    @Operation(summary = "Создать заказ (требуется авторизация)")
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest request,
                                        @AuthenticationPrincipal UserDetails principal) {
        Long userId = userDetailsService.loadUserEntityByUsername(principal.getUsername()).getUserId();
        request.setUserId(userId);
        return ResponseEntity.ok(orderService.create(request));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Заказы пользователя")
    public List<Order> getByUser(@PathVariable Long userId) {
        return orderService.getByUser(userId);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Заказы по статусу")
    public List<Order> getByStatus(@PathVariable String status) {
        return orderService.getByStatus(status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Заказ по ID")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return orderService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
