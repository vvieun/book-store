package ru.bmstu.iu7.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long bookId;
    private String title;
    private String isbn;
    private BigDecimal price;
    private String description;
    private Integer pages;
    private String publicationDate;
    private Double avgRating;
    private Integer ratingCount;
    private String publisherName;
    private Set<String> authors;
    private Set<String> categories;
}
