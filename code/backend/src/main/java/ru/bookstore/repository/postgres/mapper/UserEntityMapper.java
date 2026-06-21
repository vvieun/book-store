package ru.bookstore.repository.postgres.mapper;

import org.springframework.stereotype.Component;
import ru.bookstore.domain.User;

@Component
public class UserEntityMapper {

    public User toDomain(ru.bookstore.repository.postgres.model.User entity) {
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

    public ru.bookstore.repository.postgres.model.User toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return new ru.bookstore.repository.postgres.model.User(
                domain.getUserId(),
                domain.getUsername(),
                domain.getEmail(),
                domain.getPasswordHash(),
                domain.getRole(),
                domain.getCreatedAt()
        );
    }
}
