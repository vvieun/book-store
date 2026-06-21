package ru.bookstore.repository.mongo.spring_data;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.bookstore.repository.mongo.model.BookDoc;

public interface BookMongoRepository extends MongoRepository<BookDoc, String> {
}

