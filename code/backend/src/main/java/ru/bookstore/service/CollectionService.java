package ru.bookstore.service;

import ru.bookstore.domain.BookCollection;

import java.util.List;

public interface CollectionService {
    List<BookCollection> getCollections();

    List<BookCollection> getMyCollections(Long userId);

    BookCollection getCollection(Long collectionId);

    BookCollection getMyCollection(Long userId, Long collectionId);

    BookCollection create(Long userId, String name, String description);

    BookCollection updateDescription(Long userId, Long collectionId, String description);

    boolean delete(Long userId, Long collectionId);

    BookCollection addBook(Long userId, Long collectionId, String isbn);

    boolean removeBook(Long userId, Long collectionId, String isbn);
}
