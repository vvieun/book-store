package ru.bmstu.iu7.bookstore.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> translateFieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Ошибка валидации",
                "message", details.isEmpty() ? "Проверьте введённые данные" : details
        ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Отсутствует обязательный параметр",
                "message", "Параметр '" + ex.getParameterName() + "' обязателен"
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Неверный тип параметра",
                "message", "Параметр '" + ex.getName() + "' имеет неверный формат"
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage();
        if (msg != null && (msg.contains("already") || msg.contains("уже"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Конфликт данных",
                    "message", msg
            ));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Внутренняя ошибка сервера",
                "message", "Произошла ошибка. Попробуйте позже."
        ));
    }

    private String translateFieldError(String field, String message) {
        if (message == null) return field + ": неверное значение";
        return switch (field) {
            case "username" -> "Имя пользователя: " + translateConstraint(message);
            case "email"    -> "Email: " + translateConstraint(message);
            case "password" -> "Пароль: " + translateConstraint(message);
            default         -> field + ": " + translateConstraint(message);
        };
    }

    private String translateConstraint(String msg) {
        if (msg.contains("must not be blank") || msg.contains("blank")) return "не может быть пустым";
        if (msg.contains("size must be between")) {
            return msg.replaceAll("size must be between (\\d+) and (\\d+)", "длина от $1 до $2 символов");
        }
        if (msg.contains("must be a well-formed email")) return "введите корректный email";
        return msg;
    }
}
