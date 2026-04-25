package ru.bmstu.iu7.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bmstu.iu7.bookstore.domain.Book;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRecommendation {
    private Book book;
    private Double score;
    private String reason;
    private RecommendationType type;
    
    public enum RecommendationType {
        COLLABORATIVE,
        CONTENT_BASED,
        HYBRID
    }
}
