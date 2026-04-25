package ru.bmstu.iu7.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bmstu.iu7.bookstore.domain.Order;
import ru.bmstu.iu7.bookstore.entity.Review;
import ru.bmstu.iu7.bookstore.repository.OrderRepository;
import ru.bmstu.iu7.bookstore.repository.ReviewRepository;
import ru.bmstu.iu7.bookstore.service.OrderService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
@Tag(name = "Moderator", description = "Функции модератора")
public class ModeratorController {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final OrderService orderService;

    @GetMapping("/orders")
    @Operation(summary = "Все заказы платформы")
    public List<Order> getAllOrders(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return orderService.getByStatus(status);
        }
        return orderRepository.findAll().stream()
                .map(o -> orderService.getById(o.getOrderId()).orElseThrow())
                .toList();
    }

    @PatchMapping("/orders/{id}/status")
    @Operation(summary = "Изменить статус заказа")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Поле status обязательно"));
        }
        List<String> allowed = List.of("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!allowed.contains(newStatus.toUpperCase())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Недопустимый статус. Допустимые: " + allowed));
        }
        ru.bmstu.iu7.bookstore.entity.Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден: " + id));
        order.setStatus(newStatus.toUpperCase());
        orderRepository.save(order);
        return ResponseEntity.ok(Map.of("message", "Статус обновлён", "status", order.getStatus()));
    }

    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Удалить отзыв (модерация)")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Отзыв не найден: " + id));
        reviewRepository.delete(review);
        return ResponseEntity.ok(Map.of("message", "Отзыв удалён"));
    }

    @GetMapping("/reviews")
    @Operation(summary = "Все отзывы платформы")
    public List<Review> getAllReviews(
            @RequestParam(required = false) Long bookId) {
        if (bookId != null) {
            return reviewRepository.findByBookBookId(bookId);
        }
        return reviewRepository.findAll();
    }

    @GetMapping("/stats")
    @Operation(summary = "Статистика платформы")
    public Map<String, Object> getPlatformStats() {
        long totalOrders = orderRepository.count();
        long totalReviews = reviewRepository.count();
        long pendingOrders = orderRepository.findByStatus("PENDING").size();
        long processingOrders = orderRepository.findByStatus("PROCESSING").size();
        long deliveredOrders = orderRepository.findByStatus("DELIVERED").size();
        long cancelledOrders = orderRepository.findByStatus("CANCELLED").size();
        return Map.of(
                "totalOrders", totalOrders,
                "totalReviews", totalReviews,
                "pendingOrders", pendingOrders,
                "processingOrders", processingOrders,
                "deliveredOrders", deliveredOrders,
                "cancelledOrders", cancelledOrders
        );
    }
}
