package ru.bmstu.iu7.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Long reviewId;
    private Long bookId;
    private String bookTitle;
    private String username;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
