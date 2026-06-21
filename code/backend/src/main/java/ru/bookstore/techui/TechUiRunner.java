package ru.bookstore.techui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("techui")
@RequiredArgsConstructor
@Slf4j
public class TechUiRunner implements ApplicationRunner {

    private final ConsoleTechUi consoleTechUi;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Запуск консольного технологического UI");
        int exitCode = consoleTechUi.run(args.getSourceArgs());
        log.info("Завершение консольного технологического UI с кодом {}", exitCode);
    }
}
