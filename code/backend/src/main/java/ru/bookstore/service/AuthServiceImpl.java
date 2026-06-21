package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bookstore.domain.User;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.service.exception.AuthenticationException;
import ru.bookstore.service.exception.ConflictException;
import ru.bookstore.service.exception.ValidationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public User login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new ValidationException("Введите логин и пароль.");
        }

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new AuthenticationException("Неверный логин или пароль."));
        if (!isPasswordValid(password, user.getPasswordHash())) {
            throw new AuthenticationException("Неверный логин или пароль.");
        }

        log.info("Пользователь вошел в систему: userId={}", user.getUserId());
        return user;
    }

    @Override
    public User register(User user, String rawPassword) {
        if (user == null || isBlank(user.getUsername()) || isBlank(user.getEmail()) || isBlank(rawPassword)) {
            throw new ValidationException("Заполните логин, email и пароль.");
        }
        if (rawPassword.length() < 6) {
            throw new ValidationException("Пароль должен быть не короче 6 символов.");
        }
        String username = user.getUsername().trim();
        String email = user.getEmail().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Пользователь с таким логином уже существует.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Пользователь с таким email уже существует.");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(sha256(rawPassword));
        newUser.setRole("CUSTOMER");
        User savedUser = userRepository.save(newUser);
        log.info("Пользователь зарегистрирован: userId={}", savedUser.getUserId());
        return savedUser;
    }

    private boolean isPasswordValid(String rawPassword, String storedHash) {
        if (isBlank(storedHash)) {
            return false;
        }
        return Objects.equals(sha256(rawPassword), storedHash);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
