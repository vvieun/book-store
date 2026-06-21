package ru.bookstore.repository.postgres.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.bookstore.domain.BookCollection;

@Component
@RequiredArgsConstructor
public class BookCollectionEntityMapper {

    private final UserEntityMapper userMapper;

    public BookCollection toDomain(ru.bookstore.repository.postgres.model.BookCollection entity) {
        if (entity == null) return null;
        BookCollection c = new BookCollection();
        c.setCollectionId(entity.getCollectionId());
        c.setOwner(userMapper.toDomain(entity.getOwner()));
        c.setName(entity.getName());
        c.setDescription(entity.getDescription());
        c.setCreatedAt(entity.getCreatedAt());
        return c;
    }

    public ru.bookstore.repository.postgres.model.BookCollection toEntity(BookCollection domain) {
        if (domain == null) return null;
        ru.bookstore.repository.postgres.model.BookCollection e = new ru.bookstore.repository.postgres.model.BookCollection();
        e.setCollectionId(domain.getCollectionId());
        e.setOwner(userMapper.toEntity(domain.getOwner()));
        e.setName(domain.getName());
        e.setDescription(domain.getDescription());
        e.setCreatedAt(domain.getCreatedAt());
        return e;
    }
}

