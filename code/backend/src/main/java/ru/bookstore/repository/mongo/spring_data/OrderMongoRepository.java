package ru.bookstore.repository.mongo.spring_data;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.bookstore.repository.mongo.model.OrderDoc;

import java.util.List;

public interface OrderMongoRepository extends MongoRepository<OrderDoc, Long> {
    List<OrderDoc> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<OrderDoc> findByStatus(String status);

    List<OrderDoc> findAllByOrderByCreatedAtDesc();
}

