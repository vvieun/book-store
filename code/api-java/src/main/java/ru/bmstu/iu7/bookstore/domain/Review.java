package ru.bmstu.iu7.bookstore.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long reviewId;
    private User user;
    private Book book;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
