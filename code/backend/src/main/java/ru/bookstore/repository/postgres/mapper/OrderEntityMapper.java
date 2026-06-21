package ru.bookstore.repository.postgres.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.bookstore.domain.Order;
import ru.bookstore.domain.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderEntityMapper {

    private final UserEntityMapper userMapper;
    private final BookEntityMapper bookMapper;

    public Order toDomain(ru.bookstore.repository.postgres.model.Order entity) {
        if (entity == null) {
            return null;
        }

        List<OrderItem> items = toDomainItems(entity.getItems());

        return new Order(
                entity.getOrderId(),
                userMapper.toDomain(entity.getUser()),
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getCreatedAt(),
                items
        );
    }

    public ru.bookstore.repository.postgres.model.Order toEntity(Order domain) {
        if (domain == null) {
            return null;
        }

        ru.bookstore.repository.postgres.model.Order order = new ru.bookstore.repository.postgres.model.Order();
        order.setOrderId(domain.getOrderId());
        order.setUser(userMapper.toEntity(domain.getUser()));
        order.setTotalAmount(domain.getTotalAmount());
        order.setStatus(domain.getStatus());
        order.setCreatedAt(domain.getCreatedAt());
        order.setItems(toEntityItems(domain.getItems(), order));
        return order;
    }

    private List<OrderItem> toDomainItems(List<ru.bookstore.repository.postgres.model.OrderItem> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(this::toDomainItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<ru.bookstore.repository.postgres.model.OrderItem> toEntityItems(List<OrderItem> domains,
                                                                        ru.bookstore.repository.postgres.model.Order parent) {
        if (domains == null || domains.isEmpty()) {
            return new ArrayList<>();
        }
        return domains.stream()
                .map(this::toEntityItem)
                .peek(item -> item.setOrder(parent))
                .collect(Collectors.toList());
    }

    private OrderItem toDomainItem(ru.bookstore.repository.postgres.model.OrderItem entity) {
        if (entity == null) {
            return null;
        }
        return new OrderItem(
                entity.getOrderItemId(),
                bookMapper.toDomain(entity.getBook()),
                entity.getQuantity(),
                entity.getPrice()
        );
    }

    private ru.bookstore.repository.postgres.model.OrderItem toEntityItem(OrderItem domain) {
        if (domain == null) {
            return null;
        }
        ru.bookstore.repository.postgres.model.OrderItem item = new ru.bookstore.repository.postgres.model.OrderItem();
        item.setOrderItemId(domain.getOrderItemId());
        item.setBook(bookMapper.toEntity(domain.getBook()));
        item.setQuantity(domain.getQuantity());
        item.setPrice(domain.getPrice());
        return item;
    }
}
