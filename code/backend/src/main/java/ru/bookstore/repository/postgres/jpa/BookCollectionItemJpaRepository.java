package ru.bookstore.repository.postgres.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bookstore.repository.postgres.model.BookCollectionItem;

import java.util.List;

public interface BookCollectionItemJpaRepository extends JpaRepository<BookCollectionItem, Long> {

    boolean existsByCollectionCollectionIdAndBookIsbn(Long collectionId, String isbn);

    void deleteByCollectionCollectionIdAndBookIsbn(Long collectionId, String isbn);

    @Query("SELECT i FROM BookCollectionItem i JOIN FETCH i.book WHERE i.collection.collectionId = :collectionId ORDER BY i.addedAt DESC")
    List<BookCollectionItem> findByCollectionIdWithBookOrderByAddedAtDesc(@Param("collectionId") Long collectionId);
}

