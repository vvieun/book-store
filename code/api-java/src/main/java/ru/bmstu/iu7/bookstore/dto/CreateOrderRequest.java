package ru.bmstu.iu7.bookstore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    @Positive
    private Long userId;

    @Valid
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull
        @Positive
        private Long bookId;
        @NotNull
        @Positive
        private Integer quantity;
    }
}
