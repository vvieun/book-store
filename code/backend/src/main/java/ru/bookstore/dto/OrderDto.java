package ru.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long orderId;
    private String status;
    private BigDecimal totalAmount;
    private Long buyerUserId;
    private String buyerUsername;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;
}
