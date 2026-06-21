package ru.bookstore.repository.postgres.mapper;

import org.springframework.stereotype.Component;
import ru.bookstore.domain.Author;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Category;
import ru.bookstore.domain.Publisher;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookEntityMapper {

    public Book toDomain(ru.bookstore.repository.postgres.model.Book entity) {
        if (entity == null) {
            return null;
        }

        Book book = new Book();
        book.setIsbn(entity.getIsbn());
        book.setTitle(entity.getTitle());
        book.setPrice(entity.getPrice());
        book.setDescription(entity.getDescription());
        book.setPages(entity.getPages());
        book.setPublicationDate(entity.getPublicationDate());
        book.setAvgRating(entity.getAvgRating());
        book.setRatingCount(entity.getRatingCount());
        book.setCreatedAt(entity.getCreatedAt());
        book.setPublisher(toDomain(entity.getPublisher()));
        book.setAuthors(toDomainAuthors(entity.getAuthors()));
        book.setCategories(toDomainCategories(entity.getCategories()));
        return book;
    }

    public ru.bookstore.repository.postgres.model.Book toEntity(Book domain) {
        if (domain == null) {
            return null;
        }

        ru.bookstore.repository.postgres.model.Book book = new ru.bookstore.repository.postgres.model.Book();
        book.setIsbn(domain.getIsbn());
        book.setTitle(domain.getTitle());
        book.setPrice(domain.getPrice());
        book.setDescription(domain.getDescription());
        book.setPages(domain.getPages());
        book.setPublicationDate(domain.getPublicationDate());
        book.setAvgRating(domain.getAvgRating());
        book.setRatingCount(domain.getRatingCount());
        book.setCreatedAt(domain.getCreatedAt());
        book.setPublisher(toEntity(domain.getPublisher()));
        book.setAuthors(toEntityAuthors(domain.getAuthors()));
        book.setCategories(toEntityCategories(domain.getCategories()));
        return book;
    }

    public Author toDomain(ru.bookstore.repository.postgres.model.Author entity) {
        if (entity == null) {
            return null;
        }
        return new Author(entity.getAuthorId(), entity.getName(), entity.getBiography(), entity.getCountry());
    }

    public ru.bookstore.repository.postgres.model.Author toEntity(Author domain) {
        if (domain == null) {
            return null;
        }
        return new ru.bookstore.repository.postgres.model.Author(
                domain.getAuthorId(),
                domain.getName(),
                domain.getBiography(),
                domain.getCountry()
        );
    }

    public Category toDomain(ru.bookstore.repository.postgres.model.Category entity) {
        if (entity == null) {
            return null;
        }
        return new Category(entity.getCategoryId(), entity.getName(), entity.getDescription());
    }

    public ru.bookstore.repository.postgres.model.Category toEntity(Category domain) {
        if (domain == null) {
            return null;
        }
        return new ru.bookstore.repository.postgres.model.Category(
                domain.getCategoryId(),
                domain.getName(),
                domain.getDescription()
        );
    }

    public Publisher toDomain(ru.bookstore.repository.postgres.model.Publisher entity) {
        if (entity == null) {
            return null;
        }
        return new Publisher(entity.getPublisherId(), entity.getName(), entity.getCountry(), entity.getWebsite());
    }

    public ru.bookstore.repository.postgres.model.Publisher toEntity(Publisher domain) {
        if (domain == null) {
            return null;
        }
        return new ru.bookstore.repository.postgres.model.Publisher(
                domain.getPublisherId(),
                domain.getName(),
                domain.getCountry(),
                domain.getWebsite()
        );
    }

    private Set<Author> toDomainAuthors(Set<ru.bookstore.repository.postgres.model.Author> entities) {
        if (entities == null || entities.isEmpty()) {
            return new HashSet<>();
        }
        return entities.stream().map(this::toDomain).collect(Collectors.toSet());
    }

    private Set<Category> toDomainCategories(Set<ru.bookstore.repository.postgres.model.Category> entities) {
        if (entities == null || entities.isEmpty()) {
            return new HashSet<>();
        }
        return entities.stream().map(this::toDomain).collect(Collectors.toSet());
    }

    private Set<ru.bookstore.repository.postgres.model.Author> toEntityAuthors(Set<Author> domains) {
        if (domains == null || domains.isEmpty()) {
            return new HashSet<>();
        }
        return domains.stream().map(this::toEntity).collect(Collectors.toSet());
    }

    private Set<ru.bookstore.repository.postgres.model.Category> toEntityCategories(Set<Category> domains) {
        if (domains == null || domains.isEmpty()) {
            return new HashSet<>();
        }
        return domains.stream().map(this::toEntity).collect(Collectors.toSet());
    }
}
