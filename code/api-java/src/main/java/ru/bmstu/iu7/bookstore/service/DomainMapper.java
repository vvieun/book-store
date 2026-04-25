package ru.bmstu.iu7.bookstore.service;

import ru.bmstu.iu7.bookstore.domain.Author;
import ru.bmstu.iu7.bookstore.domain.Book;
import ru.bmstu.iu7.bookstore.domain.Category;
import ru.bmstu.iu7.bookstore.domain.Order;
import ru.bmstu.iu7.bookstore.domain.OrderItem;
import ru.bmstu.iu7.bookstore.domain.Publisher;
import ru.bmstu.iu7.bookstore.domain.Review;
import ru.bmstu.iu7.bookstore.domain.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class DomainMapper {

    private DomainMapper() {
    }

    public static Book toDomain(ru.bmstu.iu7.bookstore.entity.Book entity) {
        if (entity == null) {
            return null;
        }

        Book book = new Book();
        book.setBookId(entity.getBookId());
        book.setTitle(entity.getTitle());
        book.setIsbn(entity.getIsbn());
        book.setPrice(entity.getPrice());
        book.setDescription(entity.getDescription());
        book.setPages(entity.getPages());
        book.setPublicationDate(entity.getPublicationDate());
        book.setAvgRating(entity.getAvgRating());
        book.setRatingCount(entity.getRatingCount());
        book.setCreatedAt(entity.getCreatedAt());
        book.setPublisher(toDomain(entity.getPublisher()));
        book.setAuthors(entity.getAuthors().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toSet()));
        book.setCategories(entity.getCategories().stream()
                .map(DomainMapper::toDomain)
                .collect(Collectors.toSet()));
        return book;
    }

    public static Author toDomain(ru.bmstu.iu7.bookstore.entity.Author entity) {
        if (entity == null) {
            return null;
        }
        return new Author(entity.getAuthorId(), entity.getName(), entity.getBiography(), entity.getCountry());
    }

    public static Category toDomain(ru.bmstu.iu7.bookstore.entity.Category entity) {
        if (entity == null) {
            return null;
        }
        return new Category(entity.getCategoryId(), entity.getName(), entity.getDescription());
    }

    public static Publisher toDomain(ru.bmstu.iu7.bookstore.entity.Publisher entity) {
        if (entity == null) {
            return null;
        }
        return new Publisher(entity.getPublisherId(), entity.getName(), entity.getCountry(), entity.getWebsite());
    }

    public static User toDomain(ru.bmstu.iu7.bookstore.entity.User entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getUserId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }

    public static Review toDomain(ru.bmstu.iu7.bookstore.entity.Review entity) {
        if (entity == null) {
            return null;
        }
        return new Review(
                entity.getReviewId(),
                toDomain(entity.getUser()),
                toDomain(entity.getBook()),
                entity.getRating(),
                entity.getComment(),
                entity.getCreatedAt()
        );
    }

    public static OrderItem toDomain(ru.bmstu.iu7.bookstore.entity.OrderItem entity) {
        if (entity == null) {
            return null;
        }
        return new OrderItem(
                entity.getOrderItemId(),
                toDomain(entity.getBook()),
                entity.getQuantity(),
                entity.getPrice()
        );
    }

    public static Order toDomain(ru.bmstu.iu7.bookstore.entity.Order entity) {
        if (entity == null) {
            return null;
        }

        List<OrderItem> items = entity.getItems().stream()
                .map(DomainMapper::toDomain)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Order(
                entity.getOrderId(),
                toDomain(entity.getUser()),
                entity.getTotalAmount(),
                entity.getStatus(),
                entity.getCreatedAt(),
                items
        );
    }
}
