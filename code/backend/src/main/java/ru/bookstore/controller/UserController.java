package ru.bookstore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.bookstore.domain.User;
import ru.bookstore.dto.UpdateUserRoleRequestDto;
import ru.bookstore.dto.UserSummaryDto;
import ru.bookstore.service.AuthSessionService;
import ru.bookstore.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private static final String TOKEN_HEADER = "X-Auth-Token";

    private final UserService userService;
    private final AuthSessionService authSessionService;

    @PatchMapping("/{userId}/role")
    public UserSummaryDto updateRole(
            @RequestHeader(value = TOKEN_HEADER, required = false) String token,
            @PathVariable Long userId,
            @RequestBody(required = false) UpdateUserRoleRequestDto request) {
        User actor = authSessionService.requireUser(token);
        requireAdmin(actor);
        if (request == null || request.getRole() == null || request.getRole().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите поле role.");
        }
        log.info("HTTP users: смена роли adminUserId={}, targetUserId={}", actor.getUserId(), userId);
        User updated = userService.updateUserRole(userId, request.getRole());
        return toSummaryDto(updated);
    }

    private UserSummaryDto toSummaryDto(User user) {
        return new UserSummaryDto(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    private void requireAdmin(User user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Недостаточно прав администратора.");
        }
    }
}
