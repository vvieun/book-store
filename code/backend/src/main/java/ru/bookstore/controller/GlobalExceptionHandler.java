package ru.bookstore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.bookstore.dto.ErrorResponseDto;
import ru.bookstore.service.exception.AuthenticationException;
import ru.bookstore.service.exception.ConflictException;
import ru.bookstore.service.exception.ResourceNotFoundException;
import ru.bookstore.service.exception.UnauthorizedException;
import ru.bookstore.service.exception.ValidationException;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(ValidationException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), false);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), false);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), false);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), false);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), false);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(status, ex.getReason(), status.is2xxSuccessful());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex) {
        log.error("Необработанное исключение в веб-слое", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера.", false);
    }

    private ResponseEntity<ErrorResponseDto> build(HttpStatus status, String message, boolean success) {
        String safeMessage = (message == null || message.isBlank()) ? status.getReasonPhrase() : message;
        if (status.is5xxServerError()) {
            log.error("HTTP {}: {}", status.value(), safeMessage);
        } else if (status.is4xxClientError()) {
            log.warn("HTTP {}: {}", status.value(), safeMessage);
        } else {
            log.info("HTTP {}: {}", status.value(), safeMessage);
        }
        return ResponseEntity.status(status).body(new ErrorResponseDto(success, safeMessage, Instant.now()));
    }
}
