package ru.bookstore.repository.postgres.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.BookCollection;
import ru.bookstore.repository.CollectionRepository;
import ru.bookstore.repository.postgres.jpa.BookCollectionItemJpaRepository;
import ru.bookstore.repository.postgres.jpa.BookCollectionJpaRepository;
import ru.bookstore.repository.postgres.jpa.BookJpaRepository;
import ru.bookstore.repository.postgres.mapper.BookCollectionEntityMapper;
import ru.bookstore.repository.postgres.mapper.BookEntityMapper;
import ru.bookstore.repository.postgres.model.BookCollectionItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("postgres")
public class CollectionRepositoryImpl implements CollectionRepository {

    private final BookCollectionJpaRepository collectionJpa;
    private final BookCollectionItemJpaRepository itemJpa;
    private final BookJpaRepository bookJpa;
    private final BookCollectionEntityMapper collectionMapper;
    private final BookEntityMapper bookMapper;

    @Override
    public List<BookCollection> findAll() {
        return collectionJpa.findAllByOrderByCreatedAtDesc().stream()
                .map(collectionMapper::toDomain)
                .toList();
    }

    @Override
    public List<BookCollection> findByOwnerUserId(Long ownerUserId) {
        return collectionJpa.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId).stream()
                .map(collectionMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<BookCollection> findById(Long collectionId) {
        return collectionJpa.findById(collectionId).map(collectionMapper::toDomain);
    }

    @Override
    public Optional<BookCollection> findByIdAndOwnerUserId(Long collectionId, Long ownerUserId) {
        return collectionJpa.findByIdAndOwnerUserId(collectionId, ownerUserId).map(collectionMapper::toDomain);
    }

    @Override
    public BookCollection save(BookCollection collection) {
        return collectionMapper.toDomain(collectionJpa.save(collectionMapper.toEntity(collection)));
    }

    @Override
    public Optional<BookCollection> updateDescriptionByIdAndOwnerUserId(Long collectionId, Long ownerUserId, String description) {
        Optional<ru.bookstore.repository.postgres.model.BookCollection> found = collectionJpa.findByIdAndOwnerUserId(collectionId, ownerUserId);
        if (found.isEmpty()) return Optional.empty();
        ru.bookstore.repository.postgres.model.BookCollection collection = found.get();
        collection.setDescription(description);
        return Optional.of(collectionMapper.toDomain(collectionJpa.save(collection)));
    }

    @Override
    public boolean deleteByIdAndOwnerUserId(Long collectionId, Long ownerUserId) {
        Optional<ru.bookstore.repository.postgres.model.BookCollection> found = collectionJpa.findByIdAndOwnerUserId(collectionId, ownerUserId);
        if (found.isEmpty()) return false;
        collectionJpa.delete(found.get());
        return true;
    }

    @Override
    public List<Book> findBooks(Long collectionId) {
        return itemJpa.findByCollectionIdWithBookOrderByAddedAtDesc(collectionId).stream()
                .map(BookCollectionItem::getBook)
                .map(bookMapper::toDomain)
                .toList();
    }

    @Override
    public boolean addBook(Long collectionId, Book book) {
        String isbn = book != null ? book.getIsbn() : null;
        if (isbn == null) return false;
        if (itemJpa.existsByCollectionCollectionIdAndBookIsbn(collectionId, isbn)) {
            return false;
        }
        BookCollectionItem item = new BookCollectionItem();
        item.setCollection(collectionJpa.getReferenceById(collectionId));
        item.setBook(bookJpa.getReferenceById(isbn));
        item.setAddedAt(LocalDateTime.now());
        itemJpa.save(item);
        return true;
    }

    @Override
    public boolean removeBook(Long collectionId, String isbn) {
        if (!itemJpa.existsByCollectionCollectionIdAndBookIsbn(collectionId, isbn)) {
            return false;
        }
        itemJpa.deleteByCollectionCollectionIdAndBookIsbn(collectionId, isbn);
        return true;
    }
}
