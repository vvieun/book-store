package ru.bookstore.repository.postgres.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.bookstore.domain.Wishlist;

@Component
@RequiredArgsConstructor
public class WishlistEntityMapper {

    private final UserEntityMapper userMapper;
    private final BookEntityMapper bookMapper;

    public Wishlist toDomain(ru.bookstore.repository.postgres.model.Wishlist entity) {
        if (entity == null) {
            return null;
        }
        return new Wishlist(
                entity.getWishlistId(),
                userMapper.toDomain(entity.getUser()),
                bookMapper.toDomain(entity.getBook()),
                entity.getAddedAt()
        );
    }

    public ru.bookstore.repository.postgres.model.Wishlist toEntity(Wishlist domain) {
        if (domain == null) {
            return null;
        }
        ru.bookstore.repository.postgres.model.Wishlist entity = new ru.bookstore.repository.postgres.model.Wishlist();
        entity.setWishlistId(domain.getWishlistId());
        entity.setUser(userMapper.toEntity(domain.getUser()));
        entity.setBook(bookMapper.toEntity(domain.getBook()));
        entity.setAddedAt(domain.getAddedAt());
        return entity;
    }
}
