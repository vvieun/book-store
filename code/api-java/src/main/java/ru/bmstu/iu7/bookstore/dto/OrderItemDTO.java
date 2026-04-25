package ru.bmstu.iu7.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long orderItemId;
    private Long bookId;
    private String bookTitle;
    private Integer quantity;
    private BigDecimal price;
}
