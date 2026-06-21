package ru.bookstore.repository;

import ru.bookstore.domain.Book;
import ru.bookstore.domain.BookCollection;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository {
    List<BookCollection> findAll();

    List<BookCollection> findByOwnerUserId(Long ownerUserId);

    Optional<BookCollection> findById(Long collectionId);

    Optional<BookCollection> findByIdAndOwnerUserId(Long collectionId, Long ownerUserId);

    BookCollection save(BookCollection collection);

    Optional<BookCollection> updateDescriptionByIdAndOwnerUserId(Long collectionId, Long ownerUserId, String description);

    boolean deleteByIdAndOwnerUserId(Long collectionId, Long ownerUserId);

    List<Book> findBooks(Long collectionId);

    boolean addBook(Long collectionId, Book book);

    boolean removeBook(Long collectionId, String isbn);
}
