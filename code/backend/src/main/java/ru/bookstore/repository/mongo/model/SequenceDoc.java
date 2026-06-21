package ru.bookstore.repository.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sequences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SequenceDoc {
    @Id
    private String id;
    private long seq;
}

