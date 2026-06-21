package ru.bookstore.repository.mongo.mapper;

import org.springframework.stereotype.Component;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Order;
import ru.bookstore.domain.OrderItem;
import ru.bookstore.domain.User;
import ru.bookstore.repository.mongo.model.OrderDoc;

import java.util.List;

@Component
public class OrderMongoMapper {

    public Order toDomain(OrderDoc doc) {
        if (doc == null) return null;
        Order o = new Order();
        o.setOrderId(doc.getOrderId());
        User u = new User();
        u.setUserId(doc.getUserId());
        o.setUser(u);
        o.setTotalAmount(doc.getTotalAmount());
        o.setStatus(doc.getStatus());
        o.setCreatedAt(doc.getCreatedAt());
        o.setItems(doc.getItems() == null ? List.of() : doc.getItems().stream().map(this::toDomain).toList());
        return o;
    }

    public OrderDoc toDoc(Order domain) {
        if (domain == null) return null;
        OrderDoc doc = new OrderDoc();
        doc.setOrderId(domain.getOrderId());
        doc.setUserId(domain.getUser() != null ? domain.getUser().getUserId() : null);
        doc.setTotalAmount(domain.getTotalAmount());
        doc.setStatus(domain.getStatus());
        doc.setCreatedAt(domain.getCreatedAt());
        doc.setItems(domain.getItems() == null ? List.of() : domain.getItems().stream().map(this::toDoc).toList());
        return doc;
    }

    private OrderItem toDomain(OrderDoc.OrderItemDoc doc) {
        OrderItem i = new OrderItem();
        Book b = new Book();
        b.setIsbn(doc.getIsbn());
        b.setTitle(doc.getTitle());
        i.setBook(b);
        i.setQuantity(doc.getQuantity());
        i.setPrice(doc.getPrice());
        return i;
    }

    private OrderDoc.OrderItemDoc toDoc(OrderItem domain) {
        String isbn = domain.getBook() != null ? domain.getBook().getIsbn() : null;
        String title = domain.getBook() != null ? domain.getBook().getTitle() : null;
        return new OrderDoc.OrderItemDoc(isbn, title, domain.getQuantity(), domain.getPrice());
    }
}

