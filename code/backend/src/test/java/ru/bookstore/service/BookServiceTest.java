package ru.bookstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Author;
import ru.bookstore.domain.Category;
import ru.bookstore.domain.Publisher;
import ru.bookstore.repository.BookRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        Author author = new Author(1L, "Лев Толстой", null, "Россия");
        Category category = new Category(1L, "Классика", null);
        Publisher publisher = new Publisher(1L, "Эксмо", "Россия", null);

        testBook = new Book();
        testBook.setIsbn("978-5-04-089604-7");
        testBook.setTitle("Война и мир");
        testBook.setPrice(BigDecimal.valueOf(599.0));
        testBook.setDescription("Роман-эпопея");
        testBook.setPages(1274);
        testBook.setPublicationDate(LocalDate.of(2020, 1, 1));
        testBook.setAvgRating(4.8);
        testBook.setRatingCount(100);
        testBook.setPublisher(publisher);
        testBook.setAuthors(Set.of(author));
        testBook.setCategories(Set.of(category));
    }

    @Test
    void testFindById_Found() {
        when(bookRepository.findByIdWithDetails("978-5-04-089604-7")).thenReturn(testBook);

        Optional<Book> result = bookService.findById("978-5-04-089604-7");

        assertTrue(result.isPresent());
        assertEquals("Война и мир", result.get().getTitle());
        assertEquals(4.8, result.get().getAvgRating());
        verify(bookRepository).findByIdWithDetails("978-5-04-089604-7");
    }

    @Test
    void testFindById_NotFound() {
        when(bookRepository.findByIdWithDetails("missing-isbn")).thenReturn(null);

        Optional<Book> result = bookService.findById("missing-isbn");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByCategory_ReturnsList() {
        when(bookRepository.findByCategoryId(1L)).thenReturn(List.of(testBook));

        List<Book> result = bookService.findByCategory(1L);

        assertEquals(1, result.size());
        assertEquals("Война и мир", result.get(0).getTitle());
        verify(bookRepository).findByCategoryId(1L);
    }

    @Test
    void testFindByCategory_EmptyCategory() {
        when(bookRepository.findByCategoryId(99L)).thenReturn(List.of());

        List<Book> result = bookService.findByCategory(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTopRated_ReturnsList() {
        when(bookRepository.findTopRatedBooks(0.0, 5, 0))
                .thenReturn(List.of(testBook));

        List<Book> result = bookService.getTopRated(5);

        assertEquals(1, result.size());
        assertEquals(4.8, result.get(0).getAvgRating());
    }

    @Test
    void testSearchBooks_ReturnsResults() {
        when(bookRepository.searchBooks("Война", 10, 0))
                .thenReturn(List.of(testBook));

        List<Book> result = bookService.searchBooks("Война", 10);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().contains("Война"));
    }

    @Test
    void testSearchBooks_NoResults() {
        when(bookRepository.searchBooks("xyz", 10, 0))
                .thenReturn(List.of());

        List<Book> result = bookService.searchBooks("xyz", 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById_ReturnsDomain() {
        when(bookRepository.findByIdWithDetails("978-5-04-089604-7")).thenReturn(testBook);

        Optional<Book> result = bookService.findById("978-5-04-089604-7");

        assertTrue(result.isPresent());
        assertEquals("978-5-04-089604-7", result.get().getIsbn());
        assertEquals("Эксмо", result.get().getPublisher().getName());
    }
}
