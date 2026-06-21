package ru.bookstore.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Review;
import ru.bookstore.domain.User;
import ru.bookstore.repository.postgres.impl.ReviewRepositoryImpl;
import ru.bookstore.repository.postgres.mapper.BookEntityMapper;
import ru.bookstore.repository.postgres.mapper.ReviewEntityMapper;
import ru.bookstore.repository.postgres.mapper.UserEntityMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("postgres")
@Import({ReviewRepositoryImpl.class, ReviewEntityMapper.class, UserEntityMapper.class, BookEntityMapper.class})
class ReviewRepositoryIntegrationTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndFindByIdShouldWork() {
        User user = toDomainUser(persistUser("user-review-1", "review1@test.local"));
        Book book = toDomainBook(persistBook("Book A", "isbn-review-a"));

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(5);
        review.setComment("Отличная книга");

        Review saved = reviewRepository.save(review);
        Optional<Review> found = reviewRepository.findById(saved.getReviewId());

        assertTrue(found.isPresent());
        assertEquals(5, found.get().getRating());
        assertEquals("Отличная книга", found.get().getComment());
    }

    @Test
    void customQueriesShouldWork() {
        User user1 = toDomainUser(persistUser("user-review-2", "review2@test.local"));
        User user2 = toDomainUser(persistUser("user-review-3", "review3@test.local"));
        Book book = toDomainBook(persistBook("Book B", "isbn-review-b"));

        reviewRepository.save(buildReview(user1, book, 4, "Хорошо"));
        reviewRepository.save(buildReview(user2, book, 2, "Так себе"));

        Review byUserAndBook = reviewRepository.findByUserIdAndBookIsbn(user1.getUserId(), book.getIsbn());
        Double avgRating = reviewRepository.getAverageRatingByBookIsbn(book.getIsbn());
        List<Review> byBook = reviewRepository.findByBookIsbn(book.getIsbn());

        assertEquals(4, byUserAndBook.getRating());
        assertEquals(3.0, avgRating);
        assertEquals(2, byBook.size());
    }

    @Test
    void deleteShouldWork() {
        User user = toDomainUser(persistUser("user-review-4", "review4@test.local"));
        Book book = toDomainBook(persistBook("Book C", "isbn-review-c"));
        Review saved = reviewRepository.save(buildReview(user, book, 3, "Нормально"));

        reviewRepository.delete(saved);

        assertTrue(reviewRepository.findById(saved.getReviewId()).isEmpty());
        assertEquals(0, reviewRepository.count());
    }

    private Review buildReview(User user, Book book, int rating, String comment) {
        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);
        return review;
    }

    private ru.bookstore.repository.postgres.model.User persistUser(String username, String email) {
        ru.bookstore.repository.postgres.model.User user = new ru.bookstore.repository.postgres.model.User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("pwd");
        user.setRole("CUSTOMER");
        return entityManager.persistAndFlush(user);
    }

    private ru.bookstore.repository.postgres.model.Book persistBook(String title, String isbn) {
        ru.bookstore.repository.postgres.model.Book book = new ru.bookstore.repository.postgres.model.Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPrice(BigDecimal.valueOf(700));
        book.setDescription("integration-test");
        book.setPages(250);
        book.setAvgRating(0.0);
        book.setRatingCount(0);
        return entityManager.persistAndFlush(book);
    }

    private User toDomainUser(ru.bookstore.repository.postgres.model.User user) {
        return new User(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    private Book toDomainBook(ru.bookstore.repository.postgres.model.Book book) {
        Book domainBook = new Book();
        domainBook.setIsbn(book.getIsbn());
        domainBook.setTitle(book.getTitle());
        domainBook.setPrice(book.getPrice());
        domainBook.setDescription(book.getDescription());
        domainBook.setPages(book.getPages());
        domainBook.setPublicationDate(book.getPublicationDate());
        domainBook.setAvgRating(book.getAvgRating());
        domainBook.setRatingCount(book.getRatingCount());
        domainBook.setCreatedAt(book.getCreatedAt());
        return domainBook;
    }
}
