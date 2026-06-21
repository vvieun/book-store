package ru.bookstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "ru.bookstore")
@Slf4j
public class Application {

    public static void main(String[] args) {
        System.setProperty("io.netty.noUnsafe", "true");
        try {
            new SpringApplicationBuilder(Application.class)
                    .web(WebApplicationType.SERVLET)
                    .logStartupInfo(false)
                    .run(args);
            log.info("Контекст приложения успешно поднят");
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "Неизвестная ошибка" : ex.getMessage();
            log.error("Критическая ошибка запуска приложения", ex);
            System.out.println("Статус: неудача");
            System.out.println("Ошибка запуска: " + message);
            System.exit(1);
        }
    }
}
