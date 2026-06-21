package ru.bookstore.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.bookstore.domain.User;
import ru.bookstore.repository.UserRepository;
import ru.bookstore.service.exception.UnauthorizedException;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class AuthSessionServiceImpl implements AuthSessionService {

    private static final String SESSION_KEY_PREFIX = "auth:session:";
    private static final Duration SESSION_TTL = Duration.ofDays(7);

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuthSessionServiceImpl(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(sessionKey(token), user.getUserId(), SESSION_TTL);
        log.info("Создана пользовательская сессия: userId={}", user.getUserId());
        return token;
    }

    @Override
    public User requireUser(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Требуется вход в систему.");
        }
        Long userId = readUserId(token);
        if (userId == null) {
            throw new UnauthorizedException("Сессия завершена. Войдите снова.");
        }
        redisTemplate.expire(sessionKey(token), SESSION_TTL);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Пользователь не найден."));
    }

    private Long readUserId(String token) {
        Object value = redisTemplate.opsForValue().get(sessionKey(token));
        if (value instanceof Long id) {
            return id;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String sessionKey(String token) {
        return SESSION_KEY_PREFIX + token;
    }
}
