package ru.bookstore.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bookstore.domain.User;
import ru.bookstore.dto.AuthResponseDto;
import ru.bookstore.dto.LoginRequestDto;
import ru.bookstore.dto.RegisterRequestDto;
import ru.bookstore.dto.UserDto;
import ru.bookstore.service.AuthService;
import ru.bookstore.service.AuthSessionService;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private static final String TOKEN_HEADER = "X-Auth-Token";

    private final AuthService authService;
    private final AuthSessionService authSessionService;

    public AuthController(AuthService authService, AuthSessionService authSessionService) {
        this.authService = authService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto request) {
        log.info("HTTP вход: попытка");
        User user = authService.login(
                request != null ? request.getUsername() : null,
                request != null ? request.getPassword() : null
        );
        String token = authSessionService.createSession(user);
        AuthResponseDto response = new AuthResponseDto(token, toUserDto(user));
        log.info("HTTP вход: ок userId={}", response.getUser().getUserId());
        return response;
    }

    @PostMapping("/register")
    public AuthResponseDto register(@RequestBody RegisterRequestDto request) {
        log.info("HTTP регистрация: попытка");
        User registrationCandidate = new User();
        registrationCandidate.setUsername(request != null ? request.getUsername() : null);
        registrationCandidate.setEmail(request != null ? request.getEmail() : null);
        User savedUser = authService.register(
                registrationCandidate,
                request != null ? request.getPassword() : null
        );
        String token = authSessionService.createSession(savedUser);
        AuthResponseDto response = new AuthResponseDto(token, toUserDto(savedUser));
        log.info("HTTP регистрация: ок userId={}", response.getUser().getUserId());
        return response;
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        User user = authSessionService.requireUser(token);
        log.info("HTTP профиль: userId={}", user.getUserId());
        return toUserDto(user);
    }

    private UserDto toUserDto(User user) {
        return new UserDto(user.getUserId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
