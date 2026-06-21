package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.BookCollection;
import ru.bookstore.domain.User;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.CollectionRepository;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.service.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BookCollection> getCollections() {
        return collectionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCollection> getMyCollections(Long userId) {
        if (userId == null) {
            throw new ValidationException("userId обязателен");
        }
        return collectionRepository.findByOwnerUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BookCollection getCollection(Long collectionId) {
        if (collectionId == null) throw new ValidationException("collectionId обязателен");

        BookCollection c = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ValidationException("Подборка не найдена"));
        c.setBooks(collectionRepository.findBooks(collectionId));
        return c;
    }

    @Override
    @Transactional(readOnly = true)
    public BookCollection getMyCollection(Long userId, Long collectionId) {
        if (userId == null) throw new ValidationException("userId обязателен");
        if (collectionId == null) throw new ValidationException("collectionId обязателен");

        BookCollection c = collectionRepository.findByIdAndOwnerUserId(collectionId, userId)
                .orElseThrow(() -> new ValidationException("Подборка не найдена"));
        c.setBooks(collectionRepository.findBooks(collectionId));
        return c;
    }

    @Override
    @Transactional
    public BookCollection create(Long userId, String name, String description) {
        if (userId == null) throw new ValidationException("userId обязателен");
        if (name == null || name.isBlank()) throw new ValidationException("name обязателен");

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));

        BookCollection c = new BookCollection();
        c.setOwner(owner);
        c.setName(name.trim());
        c.setDescription(description != null ? description.trim() : null);
        c.setCreatedAt(LocalDateTime.now());

        BookCollection saved = collectionRepository.save(c);
        log.info("Подборки: создана collectionId={}, userId={}", saved.getCollectionId(), userId);
        return saved;
    }

    @Override
    @Transactional
    public BookCollection updateDescription(Long userId, Long collectionId, String description) {
        if (userId == null) throw new ValidationException("userId обязателен");
        if (collectionId == null) throw new ValidationException("collectionId обязателен");

        String value = description != null && !description.isBlank() ? description.trim() : null;
        BookCollection updated = collectionRepository.updateDescriptionByIdAndOwnerUserId(collectionId, userId, value)
                .orElseThrow(() -> new ValidationException("Подборка не найдена"));
        updated.setBooks(collectionRepository.findBooks(collectionId));
        log.info("Подборки: описание обновлено collectionId={}, userId={}", collectionId, userId);
        return updated;
    }

    @Override
    @Transactional
    public boolean delete(Long userId, Long collectionId) {
        if (userId == null) throw new ValidationException("userId обязателен");
        if (collectionId == null) throw new ValidationException("collectionId обязателен");
        boolean deleted = collectionRepository.deleteByIdAndOwnerUserId(collectionId, userId);
        log.info("Подборки: удаление collectionId={}, userId={}, ok={}", collectionId, userId, deleted);
        return deleted;
    }

    @Override
    @Transactional
    public BookCollection addBook(Long userId, Long collectionId, String isbn) {
        if (userId == null) throw new ValidationException("userId обязателен");
        if (collectionId == null) throw new ValidationException("collectionId обязателен");
        if (isbn == null || isbn.isBlank()) throw new ValidationException("isbn обязателен");

        // проверка владения
        collectionRepository.findByIdAndOwnerUserId(collectionId, userId)
                .orElseThrow(() -> new ValidationException("Подборка не найдена"));

        Book book = bookRepository.findById(isbn.trim())
                .orElseThrow(() -> new ValidationException("Книга не найдена: " + isbn));

        boolean added = collectionRepository.addBook(collectionId, book);
        log.info("Подборки: +book collectionId={}, isbn={}, ok={}", collectionId, isbn, added);
        return getMyCollection(userId, collectionId);
    }

    @Override
    @Transactional
    public boolean removeBook(Long userId, Long collectionId, String isbn) {
        if (userId == null) throw new ValidationException("userId обязателен");
        if (collectionId == null) throw new ValidationException("collectionId обязателен");
        if (isbn == null || isbn.isBlank()) throw new ValidationException("isbn обязателен");

        // проверка владения
        collectionRepository.findByIdAndOwnerUserId(collectionId, userId)
                .orElseThrow(() -> new ValidationException("Подборка не найдена"));

        boolean removed = collectionRepository.removeBook(collectionId, isbn.trim());
        log.info("Подборки: -book collectionId={}, isbn={}, ok={}", collectionId, isbn, removed);
        return removed;
    }
}
