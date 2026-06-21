package ru.bookstore.repository.postgres.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bookstore.repository.postgres.model.BookCollection;

import java.util.List;
import java.util.Optional;

public interface BookCollectionJpaRepository extends JpaRepository<BookCollection, Long> {

    List<BookCollection> findAllByOrderByCreatedAtDesc();

    List<BookCollection> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

    @Query("SELECT c FROM BookCollection c WHERE c.collectionId = :id AND c.owner.userId = :ownerUserId")
    Optional<BookCollection> findByIdAndOwnerUserId(@Param("id") Long id, @Param("ownerUserId") Long ownerUserId);
}
