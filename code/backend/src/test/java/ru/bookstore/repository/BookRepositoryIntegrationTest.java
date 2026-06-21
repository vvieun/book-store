package ru.bookstore.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.bookstore.domain.Book;
import ru.bookstore.repository.postgres.impl.BookRepositoryImpl;
import ru.bookstore.repository.postgres.mapper.BookEntityMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("postgres")
@Import({BookRepositoryImpl.class, BookEntityMapper.class})
class BookRepositoryIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void saveAndFindByIdShouldWork() {
        Book saved = bookRepository.save(buildBook("Чистая архитектура", "978-5-00100-538-9", 950));

        Optional<Book> found = bookRepository.findById(saved.getIsbn());
        assertTrue(found.isPresent());
        assertEquals("Чистая архитектура", found.get().getTitle());
    }

    @Test
    void saveExistingShouldUpdateBook() {
        Book saved = bookRepository.save(buildBook("DDD", "978-0-321-12521-7", 1200));
        saved.setTitle("Domain-Driven Design");
        saved.setAvgRating(4.9);

        bookRepository.save(saved);
        Optional<Book> found = bookRepository.findById(saved.getIsbn());

        assertTrue(found.isPresent());
        assertEquals("Domain-Driven Design", found.get().getTitle());
        assertEquals(4.9, found.get().getAvgRating());
    }

    @Test
    void deleteByIdShouldRemoveBook() {
        Book saved = bookRepository.save(buildBook("Refactoring", "978-0-201-48567-7", 1300));

        bookRepository.deleteById(saved.getIsbn());

        assertFalse(bookRepository.existsById(saved.getIsbn()));
    }

    private Book buildBook(String title, String isbn, int price) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPrice(BigDecimal.valueOf(price));
        book.setDescription("integration-test");
        book.setPages(300);
        book.setPublicationDate(LocalDate.of(2024, 1, 1));
        book.setAvgRating(4.5);
        book.setRatingCount(10);
        return book;
    }
}
