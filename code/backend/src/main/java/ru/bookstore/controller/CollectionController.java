package ru.bookstore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.BookCollection;
import ru.bookstore.domain.User;
import ru.bookstore.dto.ActionResponseDto;
import ru.bookstore.dto.CollectionBookItemDto;
import ru.bookstore.dto.CollectionBookUpdateRequestDto;
import ru.bookstore.dto.CollectionDetailsDto;
import ru.bookstore.dto.CollectionDto;
import ru.bookstore.dto.CreateCollectionRequestDto;
import ru.bookstore.dto.UpdateCollectionDescriptionRequestDto;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.service.AuthSessionService;
import ru.bookstore.service.CollectionService;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@Slf4j
public class CollectionController {

    private static final String TOKEN_HEADER = "X-Auth-Token";

    private final CollectionService collectionService;
    private final AuthSessionService authSessionService;
    private final UserRepository userRepository;

    public CollectionController(CollectionService collectionService, AuthSessionService authSessionService, UserRepository userRepository) {
        this.collectionService = collectionService;
        this.authSessionService = authSessionService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<CollectionDto> myCollections(@RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        authSessionService.requireUser(token);
        return collectionService.getCollections().stream().map(this::toDto).toList();
    }

    @PostMapping
    public CollectionDto create(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                @RequestBody CreateCollectionRequestDto request) {
        User user = authSessionService.requireUser(token);
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name обязателен");
        }
        BookCollection saved = collectionService.create(user.getUserId(), request.getName(), request.getDescription());
        return toDto(saved);
    }

    @GetMapping("/{collectionId}")
    public CollectionDetailsDto get(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                    @PathVariable Long collectionId) {
        authSessionService.requireUser(token);
        BookCollection c = collectionService.getCollection(collectionId);
        return toDetailsDto(c);
    }

    @PatchMapping("/{collectionId}/description")
    public CollectionDetailsDto updateDescription(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                                  @PathVariable Long collectionId,
                                                  @RequestBody UpdateCollectionDescriptionRequestDto request) {
        User user = authSessionService.requireUser(token);
        String description = request != null ? request.getDescription() : null;
        BookCollection c = collectionService.updateDescription(user.getUserId(), collectionId, description);
        return toDetailsDto(c);
    }

    @DeleteMapping("/{collectionId}")
    public ActionResponseDto delete(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                    @PathVariable Long collectionId) {
        User user = authSessionService.requireUser(token);
        boolean deleted = collectionService.delete(user.getUserId(), collectionId);
        return new ActionResponseDto(deleted, deleted ? "Удалено" : "Не найдено");
    }

    @PostMapping("/{collectionId}/books")
    public CollectionDetailsDto addBook(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                        @PathVariable Long collectionId,
                                        @RequestBody CollectionBookUpdateRequestDto request) {
        User user = authSessionService.requireUser(token);
        if (request == null || request.getIsbn() == null || request.getIsbn().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isbn обязателен");
        }
        BookCollection c = collectionService.addBook(user.getUserId(), collectionId, request.getIsbn());
        return toDetailsDto(c);
    }

    @DeleteMapping("/{collectionId}/books/{isbn}")
    public ActionResponseDto removeBook(@RequestHeader(value = TOKEN_HEADER, required = false) String token,
                                        @PathVariable Long collectionId,
                                        @PathVariable String isbn) {
        User user = authSessionService.requireUser(token);
        if (isbn == null || isbn.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isbn обязателен");
        }
        boolean removed = collectionService.removeBook(user.getUserId(), collectionId, isbn);
        return new ActionResponseDto(removed, removed ? "Удалено" : "Не найдено");
    }

    private CollectionDto toDto(BookCollection c) {
        return new CollectionDto(
                c.getCollectionId(),
                c.getOwner() != null ? c.getOwner().getUserId() : null,
                ownerUsername(c),
                c.getName(),
                c.getDescription()
        );
    }

    private CollectionDetailsDto toDetailsDto(BookCollection c) {
        List<CollectionBookItemDto> items = (c.getBooks() == null ? List.<Book>of() : c.getBooks()).stream()
                .map(b -> new CollectionBookItemDto(b.getIsbn(), b.getTitle()))
                .toList();
        return new CollectionDetailsDto(
                c.getCollectionId(),
                c.getOwner() != null ? c.getOwner().getUserId() : null,
                ownerUsername(c),
                c.getName(),
                c.getDescription(),
                items
        );
    }

    private String ownerUsername(BookCollection c) {
        if (c.getOwner() == null) {
            return null;
        }
        if (c.getOwner().getUsername() != null && !c.getOwner().getUsername().isBlank()) {
            return c.getOwner().getUsername();
        }
        Long ownerUserId = c.getOwner().getUserId();
        if (ownerUserId == null) {
            return null;
        }
        return userRepository.findById(ownerUserId)
                .map(User::getUsername)
                .orElse(null);
    }
}
