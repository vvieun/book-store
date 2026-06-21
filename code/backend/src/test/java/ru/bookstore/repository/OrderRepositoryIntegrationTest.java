package ru.bookstore.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.bookstore.domain.Order;
import ru.bookstore.domain.User;
import ru.bookstore.repository.postgres.impl.OrderRepositoryImpl;
import ru.bookstore.repository.postgres.mapper.BookEntityMapper;
import ru.bookstore.repository.postgres.mapper.OrderEntityMapper;
import ru.bookstore.repository.postgres.mapper.UserEntityMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("postgres")
@Import({OrderRepositoryImpl.class, OrderEntityMapper.class, UserEntityMapper.class, BookEntityMapper.class})
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndFindByIdShouldWork() {
        User user = toDomainUser(persistUser("order-user-1", "order1@test.local"));
        Order saved = orderRepository.save(buildOrder(user, BigDecimal.valueOf(1500), "PENDING"));

        Optional<Order> found = orderRepository.findById(saved.getOrderId());

        assertTrue(found.isPresent());
        assertEquals("PENDING", found.get().getStatus());
        assertEquals(BigDecimal.valueOf(1500).setScale(2), found.get().getTotalAmount().setScale(2));
    }

    @Test
    void findByUserShouldWork() {
        User user = toDomainUser(persistUser("order-user-2", "order2@test.local"));
        orderRepository.save(buildOrder(user, BigDecimal.valueOf(800), "PENDING"));
        orderRepository.save(buildOrder(user, BigDecimal.valueOf(1200), "PAID"));

        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());

        assertEquals(2, orders.size());
    }

    @Test
    void findByStatusShouldWork() {
        User user = toDomainUser(persistUser("order-user-3", "order3@test.local"));
        orderRepository.save(buildOrder(user, BigDecimal.valueOf(500), "PENDING"));
        orderRepository.save(buildOrder(user, BigDecimal.valueOf(900), "PAID"));
        orderRepository.save(buildOrder(user, BigDecimal.valueOf(1300), "PAID"));

        List<Order> paidOrders = orderRepository.findByStatus("PAID");

        assertEquals(2, paidOrders.size());
        assertEquals(3, orderRepository.count());
    }

    private Order buildOrder(User user, BigDecimal amount, String status) {
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(amount);
        order.setStatus(status);
        return order;
    }

    private ru.bookstore.repository.postgres.model.User persistUser(String username, String email) {
        ru.bookstore.repository.postgres.model.User user = new ru.bookstore.repository.postgres.model.User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("pwd");
        user.setRole("CUSTOMER");
        return entityManager.persistAndFlush(user);
    }

    private User toDomainUser(ru.bookstore.repository.postgres.model.User user) {
        return new User(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
