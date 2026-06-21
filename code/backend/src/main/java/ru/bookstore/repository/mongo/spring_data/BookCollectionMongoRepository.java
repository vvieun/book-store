package ru.bookstore.repository.mongo.spring_data;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.bookstore.repository.mongo.model.BookCollectionDoc;

import java.util.List;
import java.util.Optional;

public interface BookCollectionMongoRepository extends MongoRepository<BookCollectionDoc, Long> {
    List<BookCollectionDoc> findAllByOrderByCreatedAtDesc();

    List<BookCollectionDoc> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

    Optional<BookCollectionDoc> findByCollectionIdAndOwnerUserId(Long collectionId, Long ownerUserId);
}
