package ru.bmstu.iu7.bookstore.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Long bookId;
    private String title;
    private String isbn;
    private Publisher publisher;
    private BigDecimal price;
    private String description;
    private Integer pages;
    private LocalDate publicationDate;
    private Double avgRating;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private Set<Author> authors = new HashSet<>();
    private Set<Category> categories = new HashSet<>();
}
