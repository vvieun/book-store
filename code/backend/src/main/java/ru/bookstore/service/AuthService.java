package ru.bookstore.service;

import ru.bookstore.domain.User;

public interface AuthService {
    User login(String username, String password);
    User register(User user, String rawPassword);
}
