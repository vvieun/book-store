package ru.bmstu.iu7.bookstore.repository.impl;

import org.springframework.stereotype.Repository;
import ru.bmstu.iu7.bookstore.entity.ReadingHistory;
import ru.bmstu.iu7.bookstore.repository.ReadingHistoryRepository;
import ru.bmstu.iu7.bookstore.repository.jpa.ReadingHistoryJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReadingHistoryRepositoryImpl implements ReadingHistoryRepository {

    private final ReadingHistoryJpaRepository jpaRepo;

    public ReadingHistoryRepositoryImpl(ReadingHistoryJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public List<ReadingHistory> findByUserUserIdOrderByReadAtDesc(Long userId) {
        return jpaRepo.findByUserUserIdOrderByReadAtDesc(userId);
    }

    @Override
    public ReadingHistory findByUserUserIdAndBookBookId(Long userId, Long bookId) {
        return jpaRepo.findByUserUserIdAndBookBookId(userId, bookId);
    }

    @Override
    public Optional<ReadingHistory> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    public ReadingHistory save(ReadingHistory readingHistory) {
        return jpaRepo.save(readingHistory);
    }
}
