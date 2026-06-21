package ru.bookstore.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
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
