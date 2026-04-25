package ru.bmstu.iu7.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.bmstu.iu7.bookstore.domain.Book;
import ru.bmstu.iu7.bookstore.security.JwtAuthFilter;
import ru.bmstu.iu7.bookstore.security.JwtUtils;
import ru.bmstu.iu7.bookstore.security.UserDetailsServiceImpl;
import ru.bmstu.iu7.bookstore.service.BookService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = BookController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Тесты REST Controller для книг")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("КЭ1: книга существует — вернуть 200 с данными")
    void testGetBookById_Found_Returns200() throws Exception {
        Book book = new Book();
        book.setBookId(1L);
        book.setTitle("Война и мир");
        book.setPrice(BigDecimal.valueOf(599.0));
        book.setAvgRating(4.8);

        when(bookService.findById(1L)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.title").value("Война и мир"))
                .andExpect(jsonPath("$.avgRating").value(4.8));
    }

    @Test
    @DisplayName("КЭ2: книга не существует — вернуть 404")
    void testGetBookById_NotFound_Returns404() throws Exception {
        when(bookService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Топ книги — вернуть список в порядке рейтинга")
    void testGetTopRated_ReturnsOrderedList() throws Exception {
        Book b1 = new Book();
        b1.setBookId(1L);
        b1.setTitle("Книга 1");
        b1.setAvgRating(4.9);

        Book b2 = new Book();
        b2.setBookId(2L);
        b2.setTitle("Книга 2");
        b2.setAvgRating(4.5);

        when(bookService.getTopRated(5)).thenReturn(List.of(b1, b2));

        mockMvc.perform(get("/api/books/top-rated").param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].avgRating").value(4.9))
                .andExpect(jsonPath("$[1].avgRating").value(4.5));
    }

    @Test
    @DisplayName("Поиск книг — вернуть список результатов")
    void testSearchBooks_ReturnsResults() throws Exception {
        Book b = new Book();
        b.setBookId(3L);
        b.setTitle("Мастер и Маргарита");

        when(bookService.searchBooks("Мастер", 20)).thenReturn(List.of(b));

        mockMvc.perform(get("/api/books/search")
                        .param("query", "Мастер")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Мастер и Маргарита"));
    }

    @Test
    @DisplayName("Книги по категории — вернуть список")
    void testGetByCategory_ReturnsList() throws Exception {
        Book b = new Book();
        b.setBookId(1L);
        b.setTitle("Классика");

        when(bookService.findByCategory(1L)).thenReturn(List.of(b));

        mockMvc.perform(get("/api/books/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
