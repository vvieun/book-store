package ru.bookstore.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCollection {
    private Long collectionId;
    private User owner;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<Book> books = new ArrayList<>();
}

