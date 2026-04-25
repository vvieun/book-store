package ru.bmstu.iu7.bookstore.repository;

import ru.bmstu.iu7.bookstore.entity.ReadingHistory;

import java.util.List;
import java.util.Optional;

public interface ReadingHistoryRepository {

    List<ReadingHistory> findByUserUserIdOrderByReadAtDesc(Long userId);

    ReadingHistory findByUserUserIdAndBookBookId(Long userId, Long bookId);

    Optional<ReadingHistory> findById(Long id);

    ReadingHistory save(ReadingHistory readingHistory);
}
