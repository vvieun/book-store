package ru.bookstore.repository.mongo.mapper;

import org.springframework.stereotype.Component;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.User;
import ru.bookstore.domain.Wishlist;
import ru.bookstore.repository.mongo.model.WishlistDoc;

@Component
public class WishlistMongoMapper {

    public Wishlist toDomain(WishlistDoc doc) {
        if (doc == null) return null;
        Wishlist w = new Wishlist();
        w.setWishlistId(doc.getWishlistId());
        User u = new User();
        u.setUserId(doc.getUserId());
        w.setUser(u);
        Book b = new Book();
        b.setIsbn(doc.getBookIsbn());
        b.setTitle(doc.getBookTitle());
        w.setBook(b);
        w.setAddedAt(doc.getAddedAt());
        return w;
    }

    public WishlistDoc toDoc(Wishlist domain) {
        if (domain == null) return null;
        WishlistDoc doc = new WishlistDoc();
        doc.setWishlistId(domain.getWishlistId());
        doc.setUserId(domain.getUser() != null ? domain.getUser().getUserId() : null);
        doc.setBookIsbn(domain.getBook() != null ? domain.getBook().getIsbn() : null);
        doc.setBookTitle(domain.getBook() != null ? domain.getBook().getTitle() : null);
        doc.setAddedAt(domain.getAddedAt());
        return doc;
    }
}

