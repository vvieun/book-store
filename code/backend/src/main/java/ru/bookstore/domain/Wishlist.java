package ru.bookstore.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {
    private Long wishlistId;
    private User user;
    private Book book;
    private LocalDateTime addedAt;
}
