package ru.bookstore.repository.mongo.mapper;

import org.springframework.stereotype.Component;
import ru.bookstore.domain.Author;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Category;
import ru.bookstore.domain.Publisher;
import ru.bookstore.repository.mongo.model.BookDoc;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookMongoMapper {

    public Book toDomain(BookDoc doc) {
        if (doc == null) return null;
        Book b = new Book();
        b.setIsbn(doc.getIsbn());
        b.setTitle(doc.getTitle());
        b.setPrice(doc.getPrice());
        b.setDescription(doc.getDescription());
        b.setPages(doc.getPages());
        b.setPublicationDate(doc.getPublicationDate());
        b.setAvgRating(doc.getAvgRating());
        b.setRatingCount(doc.getRatingCount());
        b.setCreatedAt(doc.getCreatedAt());
        b.setPublisher(toDomain(doc.getPublisher()));
        b.setAuthors(toDomainAuthors(doc.getAuthors()));
        b.setCategories(toDomainCategories(doc.getCategories()));
        return b;
    }

    public BookDoc toDoc(Book domain) {
        if (domain == null) return null;
        BookDoc doc = new BookDoc();
        doc.setIsbn(domain.getIsbn());
        doc.setTitle(domain.getTitle());
        doc.setPrice(domain.getPrice());
        doc.setDescription(domain.getDescription());
        doc.setPages(domain.getPages());
        doc.setPublicationDate(domain.getPublicationDate());
        doc.setAvgRating(domain.getAvgRating());
        doc.setRatingCount(domain.getRatingCount());
        doc.setCreatedAt(domain.getCreatedAt());
        doc.setPublisher(toDoc(domain.getPublisher()));
        doc.setAuthors(toDocAuthors(domain.getAuthors()));
        doc.setCategories(toDocCategories(domain.getCategories()));
        return doc;
    }

    private Publisher toDomain(BookDoc.PublisherDoc doc) {
        if (doc == null) return null;
        return new Publisher(doc.getPublisherId(), doc.getName(), doc.getCountry(), doc.getWebsite());
    }

    private BookDoc.PublisherDoc toDoc(Publisher domain) {
        if (domain == null) return null;
        return new BookDoc.PublisherDoc(domain.getPublisherId(), domain.getName(), domain.getCountry(), domain.getWebsite());
    }

    private Set<Author> toDomainAuthors(Set<BookDoc.AuthorDoc> docs) {
        if (docs == null || docs.isEmpty()) return new HashSet<>();
        return docs.stream()
                .map(a -> new Author(a.getAuthorId(), a.getName(), a.getBiography(), a.getCountry()))
                .collect(Collectors.toSet());
    }

    private Set<Category> toDomainCategories(Set<BookDoc.CategoryDoc> docs) {
        if (docs == null || docs.isEmpty()) return new HashSet<>();
        return docs.stream()
                .map(c -> new Category(c.getCategoryId(), c.getName(), c.getDescription()))
                .collect(Collectors.toSet());
    }

    private Set<BookDoc.AuthorDoc> toDocAuthors(Set<Author> domains) {
        if (domains == null || domains.isEmpty()) return new HashSet<>();
        return domains.stream()
                .map(a -> new BookDoc.AuthorDoc(a.getAuthorId(), a.getName(), a.getBiography(), a.getCountry()))
                .collect(Collectors.toSet());
    }

    private Set<BookDoc.CategoryDoc> toDocCategories(Set<Category> domains) {
        if (domains == null || domains.isEmpty()) return new HashSet<>();
        return domains.stream()
                .map(c -> new BookDoc.CategoryDoc(c.getCategoryId(), c.getName(), c.getDescription()))
                .collect(Collectors.toSet());
    }
}

