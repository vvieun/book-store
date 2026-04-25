package ru.bmstu.iu7.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bmstu.iu7.bookstore.domain.Book;
import ru.bmstu.iu7.bookstore.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book management endpoints")
public class BookController {
    
    private final BookService bookService;
    
    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get books by category")
    public ResponseEntity<List<Book>> getBooksByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(bookService.findByCategory(categoryId));
    }
    
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated books")
    public ResponseEntity<List<Book>> getTopRated(
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(bookService.getTopRated(count));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search books")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(bookService.searchBooks(query, limit));
    }
}
