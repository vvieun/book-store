package ru.bookstore.repository.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "idx_order_user_created", def = "{'userId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_order_status", def = "{'status': 1, 'createdAt': -1}")
})
public class OrderDoc {
    @Id
    private Long orderId;

    @Indexed
    private Long userId;

    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    private List<OrderItemDoc> items = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDoc {
        private String isbn;
        private String title;
        private Integer quantity;
        private BigDecimal price;
    }
}

