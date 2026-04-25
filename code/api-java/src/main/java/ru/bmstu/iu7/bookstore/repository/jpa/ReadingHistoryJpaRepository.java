package ru.bmstu.iu7.bookstore.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bmstu.iu7.bookstore.entity.ReadingHistory;

import java.util.List;

public interface ReadingHistoryJpaRepository extends JpaRepository<ReadingHistory, Long> {

    List<ReadingHistory> findByUserUserIdOrderByReadAtDesc(Long userId);

    ReadingHistory findByUserUserIdAndBookBookId(Long userId, Long bookId);
}
