package ru.bookstore.repository.mongo.mapper;

import org.springframework.stereotype.Component;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.BookCollection;
import ru.bookstore.domain.User;
import ru.bookstore.repository.mongo.model.BookCollectionDoc;

import java.util.ArrayList;

@Component
public class BookCollectionMongoMapper {

    public BookCollection toDomain(BookCollectionDoc doc) {
        if (doc == null) return null;
        BookCollection c = new BookCollection();
        c.setCollectionId(doc.getCollectionId());
        User owner = new User();
        owner.setUserId(doc.getOwnerUserId());
        c.setOwner(owner);
        c.setName(doc.getName());
        c.setDescription(doc.getDescription());
        c.setCreatedAt(doc.getCreatedAt());
        c.setBooks(new ArrayList<>());
        if (doc.getBooks() != null) {
            for (BookCollectionDoc.BookRef ref : doc.getBooks()) {
                Book b = new Book();
                b.setIsbn(ref.getIsbn());
                b.setTitle(ref.getTitle());
                c.getBooks().add(b);
            }
        }
        return c;
    }

    public BookCollectionDoc toDoc(BookCollection domain) {
        if (domain == null) return null;
        BookCollectionDoc doc = new BookCollectionDoc();
        doc.setCollectionId(domain.getCollectionId());
        doc.setOwnerUserId(domain.getOwner() != null ? domain.getOwner().getUserId() : null);
        doc.setName(domain.getName());
        doc.setDescription(domain.getDescription());
        doc.setCreatedAt(domain.getCreatedAt());
        if (domain.getBooks() != null) {
            doc.setBooks(domain.getBooks().stream()
                    .map(b -> new BookCollectionDoc.BookRef(b.getIsbn(), b.getTitle(), null))
                    .toList());
        }
        return doc;
    }
}

