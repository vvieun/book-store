package ru.bmstu.iu7.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bmstu.iu7.bookstore.entity.*;
import ru.bmstu.iu7.bookstore.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Функции администратора")
public class AdminController {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/users")
    @Operation(summary = "Все пользователи")
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> Map.of(
                        "userId",    (Object) u.getUserId(),
                        "username",  u.getUsername(),
                        "email",     u.getEmail(),
                        "role",      u.getRole(),
                        "createdAt", String.valueOf(u.getCreatedAt())
                ))
                .toList();
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Изменить роль пользователя")
    public ResponseEntity<?> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        List<String> allowed = List.of("CUSTOMER", "MODERATOR", "ADMIN");
        if (newRole == null || !allowed.contains(newRole.toUpperCase())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Допустимые роли: " + allowed));
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));
        user.setRole(newRole.toUpperCase());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Роль обновлена", "role", user.getRole()));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Пользователь удалён"));
    }

    @PostMapping("/books")
    @Operation(summary = "Добавить книгу")
    public ResponseEntity<?> createBook(@RequestBody Map<String, Object> body) {
        Book book = new Book();
        book.setTitle((String) body.get("title"));
        book.setIsbn((String) body.get("isbn"));
        book.setDescription((String) body.get("description"));
        if (body.get("price") != null)
            book.setPrice(new BigDecimal(body.get("price").toString()));
        if (body.get("pages") != null)
            book.setPages(Integer.parseInt(body.get("pages").toString()));
        book.setAvgRating(0.0);
        book.setRatingCount(0);
        Book saved = bookRepository.save(book);
        return ResponseEntity.ok(Map.of("bookId", saved.getBookId(), "message", "Книга создана"));
    }

    @PutMapping("/books/{id}")
    @Operation(summary = "Обновить книгу")
    public ResponseEntity<?> updateBook(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Книга не найдена: " + id));
        if (body.containsKey("title"))       book.setTitle((String) body.get("title"));
        if (body.containsKey("isbn"))        book.setIsbn((String) body.get("isbn"));
        if (body.containsKey("description")) book.setDescription((String) body.get("description"));
        if (body.containsKey("price"))       book.setPrice(new BigDecimal(body.get("price").toString()));
        if (body.containsKey("pages"))       book.setPages(Integer.parseInt(body.get("pages").toString()));
        bookRepository.save(book);
        return ResponseEntity.ok(Map.of("message", "Книга обновлена"));
    }

    @DeleteMapping("/books/{id}")
    @Operation(summary = "Удалить книгу")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) return ResponseEntity.notFound().build();
        bookRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Книга удалена"));
    }

    @GetMapping("/authors")
    @Operation(summary = "Все авторы")
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    @PostMapping("/authors")
    @Operation(summary = "Добавить автора")
    public ResponseEntity<?> createAuthor(@RequestBody Map<String, String> body) {
        Author author = new Author();
        author.setName(body.get("name"));
        author.setBiography(body.get("biography"));
        author.setCountry(body.get("country"));
        Author saved = authorRepository.save(author);
        return ResponseEntity.ok(Map.of("authorId", saved.getAuthorId(), "message", "Автор создан"));
    }

    @PutMapping("/authors/{id}")
    @Operation(summary = "Обновить автора")
    public ResponseEntity<?> updateAuthor(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Автор не найден: " + id));
        if (body.containsKey("name"))      author.setName(body.get("name"));
        if (body.containsKey("biography")) author.setBiography(body.get("biography"));
        if (body.containsKey("country"))   author.setCountry(body.get("country"));
        authorRepository.save(author);
        return ResponseEntity.ok(Map.of("message", "Автор обновлён"));
    }

    @DeleteMapping("/authors/{id}")
    @Operation(summary = "Удалить автора")
    public ResponseEntity<?> deleteAuthor(@PathVariable Long id) {
        if (!authorRepository.existsById(id)) return ResponseEntity.notFound().build();
        authorRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Автор удалён"));
    }

    @GetMapping("/publishers")
    @Operation(summary = "Все издательства")
    public List<Publisher> getAllPublishers() {
        return bookRepository.findAll().stream()
                .map(Book::getPublisher)
                .filter(p -> p != null)
                .distinct()
                .toList();
    }

    @GetMapping("/categories")
    @Operation(summary = "Все категории")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping("/categories")
    @Operation(summary = "Добавить категорию")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> body) {
        Category cat = new Category();
        cat.setName(body.get("name"));
        cat.setDescription(body.get("description"));
        Category saved = categoryRepository.save(cat);
        return ResponseEntity.ok(Map.of("categoryId", saved.getCategoryId(), "message", "Категория создана"));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Обновить категорию")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + id));
        if (body.containsKey("name"))        cat.setName(body.get("name"));
        if (body.containsKey("description")) cat.setDescription(body.get("description"));
        categoryRepository.save(cat);
        return ResponseEntity.ok(Map.of("message", "Категория обновлена"));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Удалить категорию")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) return ResponseEntity.notFound().build();
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Категория удалена"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Общая статистика системы")
    public Map<String, Object> getAdminStats() {
        return Map.of(
                "totalUsers",     userRepository.count(),
                "totalBooks",     bookRepository.count(),
                "totalAuthors",   authorRepository.count(),
                "totalCategories",categoryRepository.count(),
                "totalReviews",   reviewRepository.count(),
                "totalOrders",    orderRepository.count()
        );
    }
}
