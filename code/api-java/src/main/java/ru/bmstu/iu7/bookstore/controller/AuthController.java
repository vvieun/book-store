package ru.bmstu.iu7.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.bmstu.iu7.bookstore.dto.AuthResponse;
import ru.bmstu.iu7.bookstore.dto.LoginRequest;
import ru.bmstu.iu7.bookstore.dto.RegisterRequest;
import ru.bmstu.iu7.bookstore.entity.User;
import ru.bmstu.iu7.bookstore.repository.UserRepository;
import ru.bmstu.iu7.bookstore.security.JwtUtils;
import ru.bmstu.iu7.bookstore.security.UserDetailsServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Регистрация и вход")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Имя пользователя уже занято"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email уже зарегистрирован"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("CUSTOMER");
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getUsername());
        User saved = userDetailsService.loadUserEntityByUsername(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, saved.getUserId(), saved.getUsername(), saved.getEmail(), saved.getRole()));
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        if (!authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Неверные учётные данные"));
        }

        String token = jwtUtils.generateToken(request.getUsername());
        User user = userDetailsService.loadUserEntityByUsername(request.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getUserId(), user.getUsername(), user.getEmail(), user.getRole()));
    }
}
