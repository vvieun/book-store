package ru.bmstu.iu7.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bmstu.iu7.bookstore.dto.CreateOrderRequest;
import ru.bmstu.iu7.bookstore.domain.Order;
import ru.bmstu.iu7.bookstore.entity.Book;
import ru.bmstu.iu7.bookstore.entity.OrderItem;
import ru.bmstu.iu7.bookstore.entity.User;
import ru.bmstu.iu7.bookstore.repository.BookRepository;
import ru.bmstu.iu7.bookstore.repository.OrderRepository;
import ru.bmstu.iu7.bookstore.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public Order create(CreateOrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        ru.bmstu.iu7.bookstore.entity.Order order = new ru.bmstu.iu7.bookstore.entity.Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException("Book not found: " + itemReq.getBookId()));
            BigDecimal itemPrice = book.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(itemPrice);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setBook(book);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(book.getPrice());
            order.getItems().add(item);
        }
        order.setTotalAmount(total);
        ru.bmstu.iu7.bookstore.entity.Order saved = orderRepository.save(order);
        log.debug("Order created: id={}, total={}", saved.getOrderId(), saved.getTotalAmount());
        return DomainMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getByUser(Long userId) {
        return orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getByStatus(String status) {
        return orderRepository.findByStatus(status).stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(DomainMapper::toDomain);
    }
}
