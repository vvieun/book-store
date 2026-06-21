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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "idx_book_title", def = "{'title': 1}"),
        @CompoundIndex(name = "idx_book_rating", def = "{'avgRating': -1, 'ratingCount': -1}")
})
public class BookDoc {
    @Id
    private String isbn;

    @Indexed
    private String title;

    private PublisherDoc publisher;
    private BigDecimal price;
    private String description;
    private Integer pages;
    private LocalDate publicationDate;
    private Double avgRating;
    private Integer ratingCount;
    private LocalDateTime createdAt;

    private Set<AuthorDoc> authors = new HashSet<>();
    private Set<CategoryDoc> categories = new HashSet<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDoc {
        private Long authorId;
        private String name;
        private String biography;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDoc {
        private Long categoryId;
        private String name;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublisherDoc {
        private Long publisherId;
        private String name;
        private String country;
        private String website;
    }
}

