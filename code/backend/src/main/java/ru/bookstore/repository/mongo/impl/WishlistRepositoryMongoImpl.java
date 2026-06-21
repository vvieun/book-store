package ru.bookstore.repository.mongo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Wishlist;
import ru.bookstore.repository.WishlistRepository;
import ru.bookstore.repository.mongo.mapper.WishlistMongoMapper;
import ru.bookstore.repository.mongo.model.WishlistDoc;
import ru.bookstore.repository.mongo.service.MongoSequenceService;
import ru.bookstore.repository.mongo.spring_data.WishlistMongoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Profile("mongo")
public class WishlistRepositoryMongoImpl implements WishlistRepository {

    private final WishlistMongoRepository mongoRepo;
    private final WishlistMongoMapper mapper;
    private final MongoSequenceService seq;

    @Override
    public List<Wishlist> findByUserUserId(Long userId) {
        return mongoRepo.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByUserIdAndBookIsbn(Long userId, String isbn) {
        return mongoRepo.existsByUserIdAndBookIsbn(userId, isbn);
    }

    @Override
    public Wishlist save(Wishlist wishlist) {
        WishlistDoc doc = mapper.toDoc(wishlist);
        if (doc.getWishlistId() == null) {
            doc.setWishlistId(seq.next("wishlist"));
        }
        if (doc.getAddedAt() == null) {
            doc.setAddedAt(LocalDateTime.now());
        }
        return mapper.toDomain(mongoRepo.save(doc));
    }

    @Override
    public boolean deleteByUserIdAndBookIsbn(Long userId, String isbn) {
        if (!mongoRepo.existsByUserIdAndBookIsbn(userId, isbn)) {
            return false;
        }
        mongoRepo.deleteByUserIdAndBookIsbn(userId, isbn);
        return true;
    }
}

