package ru.bookstore.repository.mongo.mapper;

import org.springframework.stereotype.Component;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Review;
import ru.bookstore.domain.User;
import ru.bookstore.repository.mongo.model.ReviewDoc;

@Component
public class ReviewMongoMapper {

    public Review toDomain(ReviewDoc doc) {
        if (doc == null) return null;
        Review r = new Review();
        r.setReviewId(doc.getReviewId());
        User u = new User();
        u.setUserId(doc.getUserId());
        u.setUsername(doc.getUsername());
        r.setUser(u);
        Book b = new Book();
        b.setIsbn(doc.getBookIsbn());
        r.setBook(b);
        r.setRating(doc.getRating());
        r.setComment(doc.getComment());
        r.setCreatedAt(doc.getCreatedAt());
        return r;
    }

    public ReviewDoc toDoc(Review domain) {
        if (domain == null) return null;
        ReviewDoc doc = new ReviewDoc();
        doc.setReviewId(domain.getReviewId());
        doc.setUserId(domain.getUser() != null ? domain.getUser().getUserId() : null);
        doc.setUsername(domain.getUser() != null ? domain.getUser().getUsername() : null);
        doc.setBookIsbn(domain.getBook() != null ? domain.getBook().getIsbn() : null);
        doc.setRating(domain.getRating());
        doc.setComment(domain.getComment());
        doc.setCreatedAt(domain.getCreatedAt());
        return doc;
    }
}

