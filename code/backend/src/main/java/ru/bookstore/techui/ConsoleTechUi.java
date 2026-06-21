package ru.bookstore.techui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Order;
import ru.bookstore.domain.OrderItem;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.domain.Review;
import ru.bookstore.domain.User;
import ru.bookstore.service.BookService;
import ru.bookstore.service.OrderService;
import ru.bookstore.service.RecommendationService;
import ru.bookstore.service.ReviewService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsoleTechUi {

    private final BookService bookService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final RecommendationService recommendationService;

    public int run(String[] args) {
        log.info("Запуск технологического UI");
        return runInteractive();
    }

    private int runInteractive() {
        printGreeting();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                printMenu();
                System.out.print("tech-ui> ");
                if (!scanner.hasNextLine()) {
                    return 0;
                }

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                    log.info("Пользователь завершил работу в UI");
                    return 0;
                }

                int code = 1;
                try {
                    if (isNumeric(line)) {
                        int choice = Integer.parseInt(line);
                        log.info("Выбран пункт меню: {}", choice);
                        if (choice == 0) {
                            log.info("Пользователь завершил работу через пункт меню");
                            return 0;
                        }
                        code = executeMenuSelection(scanner, choice);
                    } else {
                        System.out.println("Введите номер пункта меню.");
                        code = 1;
                    }
                } catch (Exception ex) {
                    log.error("Ошибка выполнения команды пользователя: {}", line, ex);
                    System.out.println("Ошибка: " + ex.getMessage());
                    code = 1;
                }
                System.out.println(code == 0 ? "Статус: успех" : "Статус: неудача");
            }
        }
    }

    private void printGreeting() {
    }

    private void printMenu() {
        System.out.println(" 0. Exit");
        System.out.println(" 1. Получить рекомендации");
        System.out.println(" 2. Оформить заказ");
        System.out.println(" 3. Оставить/обновить отзыв");
        System.out.println(" 4. Удалить отзыв");
        System.out.println(" 5. Сбросить кэш рекомендаций");
    }

    private int executeMenuSelection(Scanner scanner, int choice) {
        return switch (choice) {
            case 0 -> 0;
            case 1 -> runGetRecommendationsScenario(scanner);
            case 2 -> runCreateOrderScenario(scanner);
            case 3 -> runCustomerReviewScenario(scanner);
            case 4 -> runModeratorDeleteReviewScenario(scanner);
            case 5 -> runAdminClearCacheScenario();
            default -> {
                System.out.println("Unknown menu item: " + choice);
                yield 2;
            }
        };
    }

    private int runGetRecommendationsScenario(Scanner scanner) {
        System.out.println("Действие: получить рекомендации");
        long userId = Long.parseLong(ask(scanner, "ID покупателя"));
        int count = Integer.parseInt(askDefault(scanner, "Сколько рекомендаций показать", "5"));
        log.info("Запрос рекомендаций: userId={}, count={}", userId, count);

        List<Recommendation> recommendations = recommendationService.getHybridRecommendations(userId, count);
        if (recommendations == null || recommendations.isEmpty()) {
            System.out.println("Персональные рекомендации не найдены, показываю популярные книги.");
            recommendations = recommendationService.getPopularRecommendations(count);
        }
        if (recommendations == null || recommendations.isEmpty()) {
            System.out.println("Нет доступных рекомендаций.");
            return 1;
        }

        System.out.println("Рекомендованные книги:");
        for (int i = 0; i < recommendations.size(); i++) {
            Recommendation rec = recommendations.get(i);
            Book book = rec.getBook();
            System.out.printf("%d) isbn=%s, title='%s', score=%.3f%n",
                    i + 1,
                    book != null ? book.getIsbn() : null,
                    book != null ? book.getTitle() : null,
                    rec.getScore());
        }

        String detailsBookInput = askOptional(scanner, "Показать детали книги (isbn, Enter - пропустить)");
        if (!detailsBookInput.isBlank()) {
            String isbn = detailsBookInput.trim();
            System.out.println("Карточка книги:");
            bookService.findById(isbn).ifPresentOrElse(
                    this::printBook,
                    () -> System.out.println("Книга не найдена: " + isbn)
            );
            System.out.println("Отзывы по книге:");
            printReviews(reviewService.getBookReviews(isbn));
        }
        return 0;
    }

    private int runCreateOrderScenario(Scanner scanner) {
        System.out.println("Действие: оформить заказ");
        long userId = Long.parseLong(ask(scanner, "ID покупателя"));
        String isbn = ask(scanner, "ISBN книги");
        int quantity = Integer.parseInt(askDefault(scanner, "Количество", "1"));
        log.info("Создание заказа: userId={}, isbn={}, quantity={}", userId, isbn, quantity);

        Order order = new Order();
        User user = new User();
        user.setUserId(userId);
        order.setUser(user);
        order.setStatus("PENDING");

        OrderItem item = new OrderItem();
        Book book = new Book();
        book.setIsbn(isbn);
        item.setBook(book);
        item.setQuantity(quantity);
        item.setPrice(BigDecimal.ZERO);
        order.setItems(List.of(item));

        Order created = orderService.create(order);
        System.out.println("Заказ оформлен:");
        printOrder(created);
        return 0;
    }

    private int runCustomerReviewScenario(Scanner scanner) {
        System.out.println("Сценарий 2: Покупатель оставляет отзыв на книгу");
        long userId = Long.parseLong(ask(scanner, "ID покупателя"));
        String isbn = ask(scanner, "ISBN книги");
        int rating = Integer.parseInt(askDefault(scanner, "Оценка (1..5)", "5"));
        String comment = askDefault(scanner, "Комментарий", "");
        log.info("Сохранение отзыва: userId={}, isbn={}, rating={}", userId, isbn, rating);

        Review review = new Review();
        User user = new User();
        user.setUserId(userId);
        Book book = new Book();
        book.setIsbn(isbn);
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);

        Review saved = reviewService.createReview(review);
        System.out.println("Отзыв сохранен/обновлен:");
        printReview(saved);
        return 0;
    }

    private int runModeratorDeleteReviewScenario(Scanner scanner) {
        System.out.println("Сценарий 3: Модератор удаляет нарушающий отзыв");
        List<Review> reviews = reviewService.getAllReviews();
        printReviews(reviews);
        long reviewId = Long.parseLong(ask(scanner, "ID отзыва для удаления"));
        log.info("Удаление отзыва: reviewId={}", reviewId);
        boolean deleted = reviewService.deleteReview(reviewId);
        System.out.println("Результат удаления: " + deleted);
        return deleted ? 0 : 1;
    }

    private int runAdminClearCacheScenario() {
        System.out.println("Сценарий 4: Администратор сбрасывает устаревшие рекомендации");
        log.info("Сброс кэша рекомендаций пользователем");
        recommendationService.clearRecommendationCaches();
        System.out.println("Кэш рекомендаций очищен.");
        return 0;
    }

    private String ask(Scanner scanner, String title) {
        while (true) {
            System.out.print(title + ": ");
            if (!scanner.hasNextLine()) {
                throw new IllegalArgumentException("Input stream is closed");
            }
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Value is required.");
        }
    }

    private String askDefault(Scanner scanner, String title, String defaultValue) {
        System.out.print(title + ": ");
        if (!scanner.hasNextLine()) {
            throw new IllegalArgumentException("Input stream is closed");
        }
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? defaultValue : value;
    }

    private String askOptional(Scanner scanner, String title) {
        System.out.print(title + ": ");
        if (!scanner.hasNextLine()) {
            throw new IllegalArgumentException("Input stream is closed");
        }
        return scanner.nextLine().trim();
    }

    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private void printBook(Book b) {
        System.out.printf("Book{isbn=%s, title='%s', rating=%s, price=%s}%n",
                b.getIsbn(), b.getTitle(), b.getAvgRating(), b.getPrice());
    }

    private void printOrder(Order o) {
        int itemCount = o.getItems() == null ? 0 : o.getItems().size();
        System.out.printf("Order{id=%s, userId=%s, status=%s, total=%s, items=%d}%n",
                o.getOrderId(),
                o.getUser() != null ? o.getUser().getUserId() : null,
                o.getStatus(),
                o.getTotalAmount(),
                itemCount);
    }

    private void printReviews(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            System.out.println("No reviews found.");
            return;
        }
        reviews.forEach(this::printReview);
    }

    private void printReview(Review r) {
        System.out.printf("Review{id=%s, userId=%s, isbn=%s, rating=%s, comment='%s'}%n",
                r.getReviewId(),
                r.getUser() != null ? r.getUser().getUserId() : null,
                r.getBook() != null ? r.getBook().getIsbn() : null,
                r.getRating(),
                r.getComment());
    }

}
