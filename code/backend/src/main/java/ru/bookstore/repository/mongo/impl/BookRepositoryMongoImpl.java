package ru.bookstore.repository.mongo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Book;
import ru.bookstore.repository.BookRepository;
import ru.bookstore.repository.mongo.mapper.BookMongoMapper;
import ru.bookstore.repository.mongo.model.BookDoc;
import ru.bookstore.repository.mongo.spring_data.BookMongoRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("mongo")
public class BookRepositoryMongoImpl implements BookRepository {

    private final BookMongoRepository mongoRepo;
    private final MongoOperations mongo;
    private final BookMongoMapper mapper;

    @Override
    public Optional<Book> findById(String isbn) {
        return mongoRepo.findById(isbn).map(mapper::toDomain);
    }

    @Override
    public Book findByIdWithDetails(String isbn) {
        return mongoRepo.findById(isbn).map(mapper::toDomain).orElse(null);
    }

    @Override
    public List<Book> findByCategoryId(Long categoryId) {
        Query q = new Query(Criteria.where("categories.categoryId").is(categoryId));
        return mongo.find(q, BookDoc.class).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Book> findTopRatedBooks(Double minRating, int limit, int offset) {
        double mr = minRating == null ? 0.0 : minRating;
        Query q = new Query(Criteria.where("avgRating").gte(mr))
                .with(Sort.by(Sort.Order.desc("avgRating"), Sort.Order.desc("ratingCount")))
                .skip(Math.max(0, offset))
                .limit(Math.max(1, limit));
        return mongo.find(q, BookDoc.class).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Book> searchBooks(String query, int limit, int offset) {
        String qStr = query == null ? "" : query.trim();
        Criteria c;
        if (qStr.isBlank()) {
            c = new Criteria();
        } else {
            c = new Criteria().orOperator(
                    Criteria.where("title").regex(qStr, "i"),
                    Criteria.where("description").regex(qStr, "i")
            );
        }
        Query q = new Query(c)
                .skip(Math.max(0, offset))
                .limit(Math.max(1, limit));
        return mongo.find(q, BookDoc.class).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Book> findAll() {
        return mongoRepo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Book save(Book book) {
        return mapper.toDomain(mongoRepo.save(mapper.toDoc(book)));
    }

    @Override
    public void deleteById(String id) {
        mongoRepo.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return mongoRepo.existsById(id);
    }

    @Override
    public long count() {
        return mongoRepo.count();
    }
}

