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

@Document(collection = "wishlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "uq_wishlist_user_book", def = "{'userId': 1, 'bookIsbn': 1}", unique = true),
        @CompoundIndex(name = "idx_wishlist_user", def = "{'userId': 1, 'addedAt': -1}")
})
public class WishlistDoc {
    @Id
    private Long wishlistId;

    @Indexed
    private Long userId;

    @Indexed
    private String bookIsbn;

    private String bookTitle;
    private LocalDateTime addedAt;
}

