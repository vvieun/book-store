package ru.bookstore.repository.mongo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.BookCollection;
import ru.bookstore.repository.CollectionRepository;
import ru.bookstore.repository.mongo.mapper.BookCollectionMongoMapper;
import ru.bookstore.repository.mongo.model.BookCollectionDoc;
import ru.bookstore.repository.mongo.service.MongoSequenceService;
import ru.bookstore.repository.mongo.spring_data.BookCollectionMongoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("mongo")
public class CollectionRepositoryMongoImpl implements CollectionRepository {

    private final BookCollectionMongoRepository mongoRepo;
    private final BookCollectionMongoMapper mapper;
    private final MongoSequenceService seq;

    @Override
    public List<BookCollection> findAll() {
        return mongoRepo.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<BookCollection> findByOwnerUserId(Long ownerUserId) {
        return mongoRepo.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<BookCollection> findById(Long collectionId) {
        return mongoRepo.findById(collectionId).map(mapper::toDomain);
    }

    @Override
    public Optional<BookCollection> findByIdAndOwnerUserId(Long collectionId, Long ownerUserId) {
        return mongoRepo.findByCollectionIdAndOwnerUserId(collectionId, ownerUserId).map(mapper::toDomain);
    }

    @Override
    public BookCollection save(BookCollection collection) {
        BookCollectionDoc doc = mapper.toDoc(collection);
        if (doc.getCollectionId() == null) {
            doc.setCollectionId(seq.next("collections"));
        }
        if (doc.getCreatedAt() == null) {
            doc.setCreatedAt(LocalDateTime.now());
        }
        if (doc.getBooks() == null) {
            doc.setBooks(new ArrayList<>());
        }
        return mapper.toDomain(mongoRepo.save(doc));
    }

    @Override
    public Optional<BookCollection> updateDescriptionByIdAndOwnerUserId(Long collectionId, Long ownerUserId, String description) {
        Optional<BookCollectionDoc> docOpt = mongoRepo.findByCollectionIdAndOwnerUserId(collectionId, ownerUserId);
        if (docOpt.isEmpty()) return Optional.empty();
        BookCollectionDoc doc = docOpt.get();
        doc.setDescription(description);
        return Optional.of(mapper.toDomain(mongoRepo.save(doc)));
    }

    @Override
    public boolean deleteByIdAndOwnerUserId(Long collectionId, Long ownerUserId) {
        Optional<BookCollectionDoc> doc = mongoRepo.findByCollectionIdAndOwnerUserId(collectionId, ownerUserId);
        if (doc.isEmpty()) return false;
        mongoRepo.delete(doc.get());
        return true;
    }

    @Override
    public List<Book> findBooks(Long collectionId) {
        Optional<BookCollectionDoc> doc = mongoRepo.findById(collectionId);
        if (doc.isEmpty() || doc.get().getBooks() == null) return List.of();
        return doc.get().getBooks().stream().map(ref -> {
            Book b = new Book();
            b.setIsbn(ref.getIsbn());
            b.setTitle(ref.getTitle());
            return b;
        }).toList();
    }

    @Override
    public boolean addBook(Long collectionId, Book book) {
        Optional<BookCollectionDoc> docOpt = mongoRepo.findById(collectionId);
        if (docOpt.isEmpty()) return false;
        BookCollectionDoc doc = docOpt.get();
        if (doc.getBooks() == null) doc.setBooks(new ArrayList<>());
        String isbn = book != null ? book.getIsbn() : null;
        if (isbn == null) return false;
        boolean exists = doc.getBooks().stream().anyMatch(b -> isbn.equals(b.getIsbn()));
        if (exists) return false;
        doc.getBooks().add(new BookCollectionDoc.BookRef(isbn, book.getTitle(), LocalDateTime.now()));
        mongoRepo.save(doc);
        return true;
    }

    @Override
    public boolean removeBook(Long collectionId, String isbn) {
        Optional<BookCollectionDoc> docOpt = mongoRepo.findById(collectionId);
        if (docOpt.isEmpty()) return false;
        BookCollectionDoc doc = docOpt.get();
        if (doc.getBooks() == null || doc.getBooks().isEmpty()) return false;
        boolean removed = doc.getBooks().removeIf(b -> isbn.equals(b.getIsbn()));
        if (!removed) return false;
        mongoRepo.save(doc);
        return true;
    }
}
