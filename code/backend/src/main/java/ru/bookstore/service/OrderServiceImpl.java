package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Order;
import ru.bookstore.domain.OrderItem;
import ru.bookstore.domain.User;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.OrderRepository;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.service.exception.ResourceNotFoundException;
import ru.bookstore.service.exception.ValidationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PENDING", "PROCESSING", "DELIVERED", "CANCELLED"
    );

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public Order create(Order order) {
        if (order == null || order.getUser() == null || order.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Order user is required");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items are required");
        }

        Long userId = order.getUser().getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        order.setUser(user);
        order.setStatus(order.getStatus() != null ? order.getStatus() : "PENDING");
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getBook() == null || item.getBook().getIsbn() == null || item.getBook().getIsbn().isBlank()) {
                throw new IllegalArgumentException("Order item book is required");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Order item quantity must be greater than zero");
            }

            String isbn = item.getBook().getIsbn();
            Book book = bookRepository.findById(isbn)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found: " + isbn));

            BigDecimal itemPrice = book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemPrice);
            item.setBook(book);
            item.setPrice(book.getPrice());
        }
        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        log.debug("Order created: id={}, total={}", savedOrder.getOrderId(), savedOrder.getTotalAmount());
        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getByUser(Long userId) {
        return orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> listAllOrdersForStaff() {
        List<Order> orders = orderRepository.findAllOrderByCreatedAtDescWithDetails();
        hydrateBuyers(orders);
        return orders;
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        if (orderId == null) {
            throw new ValidationException("Укажите идентификатор заказа.");
        }
        String normalized = normalizeStatus(newStatus);
        if (normalized == null) {
            throw new ValidationException("Укажите статус заказа.");
        }
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден: " + orderId));
        hydrateBuyer(order);
        order.setStatus(normalized);
        Order saved = orderRepository.save(order);
        log.info("Order status updated: orderId={}, status={}", orderId, normalized);
        return saved;
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String s = status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(s)) {
            throw new ValidationException("Допустимые статусы: PENDING, PROCESSING, DELIVERED, CANCELLED.");
        }
        return s;
    }

    private void hydrateBuyers(List<Order> orders) {
        for (Order o : orders) {
            hydrateBuyer(o);
        }
    }

    private void hydrateBuyer(Order order) {
        if (order == null || order.getUser() == null || order.getUser().getUserId() == null) {
            return;
        }
        Long uid = order.getUser().getUserId();
        if (order.getUser().getUsername() != null && !order.getUser().getUsername().isBlank()) {
            return;
        }
        userRepository.findById(uid).ifPresent(order::setUser);
    }
}
