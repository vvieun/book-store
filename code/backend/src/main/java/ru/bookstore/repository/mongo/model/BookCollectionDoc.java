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
import java.util.ArrayList;
import java.util.List;

@Document(collection = "collections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "idx_collections_owner_created", def = "{'ownerUserId': 1, 'createdAt': -1}")
})
public class BookCollectionDoc {
    @Id
    private Long collectionId;

    @Indexed
    private Long ownerUserId;

    private String name;
    private String description;
    private LocalDateTime createdAt;

    private List<BookRef> books = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookRef {
        private String isbn;
        private String title;
        private LocalDateTime addedAt;
    }
}

