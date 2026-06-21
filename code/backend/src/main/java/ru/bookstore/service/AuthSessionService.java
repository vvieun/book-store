package ru.bookstore.service;

import ru.bookstore.domain.User;

public interface AuthSessionService {
    String createSession(User user);
    User requireUser(String token);
}
