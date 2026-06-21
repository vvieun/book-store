package ru.bookstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Order;
import ru.bookstore.domain.OrderItem;
import ru.bookstore.domain.User;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.OrderRepository;
import ru.bookstore.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("buyer");

        testBook = new Book();
        testBook.setIsbn("isbn-10");
        testBook.setTitle("Clean Code");
        testBook.setPrice(BigDecimal.valueOf(500.0));
    }

    private Order buildCreateOrder(Long userId, String isbn, int qty) {
        User user = new User();
        user.setUserId(userId);

        Book book = new Book();
        book.setIsbn(isbn);

        OrderItem item = new OrderItem();
        item.setBook(book);
        item.setQuantity(qty);

        Order order = new Order();
        order.setUser(user);
        order.setItems(List.of(item));
        return order;
    }

    private Order buildSavedOrder() {
        Order order = new Order();
        order.setOrderId(100L);
        order.setUser(testUser);
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.valueOf(1000.0));
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        return order;
    }

    @Test
    void testCreate_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById("isbn-10")).thenReturn(Optional.of(testBook));

        Order saved = buildSavedOrder();
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        Order result = orderService.create(buildCreateOrder(1L, "isbn-10", 2));

        assertNotNull(result);
        assertEquals(100L, result.getOrderId());
        assertEquals("PENDING", result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCreate_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.create(buildCreateOrder(99L, "isbn-10", 1)));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testCreate_BookNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById("isbn-99")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.create(buildCreateOrder(1L, "isbn-99", 1)));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testGetByUser_ReturnsList() {
        Order o1 = buildSavedOrder();
        Order o2 = buildSavedOrder();
        o2.setOrderId(101L);
        when(orderRepository.findByUserUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(o1, o2));

        List<Order> result = orderService.getByUser(1L);

        assertEquals(2, result.size());
        verify(orderRepository).findByUserUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testGetByUser_Empty() {
        when(orderRepository.findByUserUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<Order> result = orderService.getByUser(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByStatus_ReturnsList() {
        when(orderRepository.findByStatus("PENDING")).thenReturn(List.of(buildSavedOrder()));

        List<Order> result = orderService.getByStatus("PENDING");

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    void testGetById_Found() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(buildSavedOrder()));

        Optional<Order> result = orderService.getById(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getOrderId());
    }

    @Test
    void testGetById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getById(999L);

        assertTrue(result.isEmpty());
    }
}
