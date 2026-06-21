package ru.bookstore.service;

import ru.bookstore.domain.User;

public interface UserService {

    User updateUserRole(Long userId, String newRole);
}
