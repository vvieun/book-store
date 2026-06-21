package ru.bookstore.repository.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "uq_review_user_book", def = "{'userId': 1, 'bookIsbn': 1}", unique = true),
        @CompoundIndex(name = "idx_review_book", def = "{'bookIsbn': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "idx_review_user", def = "{'userId': 1, 'createdAt': -1}")
})
public class ReviewDoc {
    @Id
    private Long reviewId;

    @Indexed
    private Long userId;

    private String username;

    @Indexed
    private String bookIsbn;

    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

