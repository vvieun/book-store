package ru.bookstore.repository.mongo.mapper;

import org.springframework.stereotype.Component;
import ru.bookstore.domain.User;
import ru.bookstore.repository.mongo.model.UserDoc;

@Component
public class UserMongoMapper {

    public User toDomain(UserDoc doc) {
        if (doc == null) return null;
        return new User(doc.getUserId(), doc.getUsername(), doc.getEmail(), doc.getPasswordHash(), doc.getRole(), doc.getCreatedAt());
    }

    public UserDoc toDoc(User domain) {
        if (domain == null) return null;
        return new UserDoc(domain.getUserId(), domain.getUsername(), domain.getEmail(), domain.getPasswordHash(), domain.getRole(), domain.getCreatedAt());
    }
}

