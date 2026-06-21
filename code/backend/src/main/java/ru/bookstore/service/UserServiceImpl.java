package ru.bookstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.domain.User;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.service.exception.ResourceNotFoundException;
import ru.bookstore.service.exception.ValidationException;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("CUSTOMER", "MODERATOR", "ADMIN");

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User updateUserRole(Long userId, String newRole) {
        if (userId == null) {
            throw new ValidationException("Укажите идентификатор пользователя.");
        }
        String normalized = normalizeRole(newRole);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + userId));

        String current = user.getRole() == null ? "" : user.getRole().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(current) && !"ADMIN".equals(normalized)) {
            if (userRepository.countByRole("ADMIN") <= 1) {
                throw new ValidationException("Нельзя снять роль ADMIN с единственного администратора.");
            }
        }

        user.setRole(normalized);
        User saved = userRepository.save(user);
        log.info("Роль пользователя обновлена: userId={}, role={}", userId, normalized);
        return saved;
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            throw new ValidationException("Укажите роль.");
        }
        String r = role.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(r)) {
            throw new ValidationException("Допустимые роли: CUSTOMER, MODERATOR, ADMIN.");
        }
        return r;
    }
}
