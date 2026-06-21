package ru.bookstore.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ru.bookstore.domain.Book;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.repository.postgres.impl.RecommendationCacheRepositoryImpl;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationCacheRepositoryIntegrationTest {

    private RecommendationCacheRepository repository;
    private final Map<String, Object> inMemoryStore = new HashMap<>();

    @BeforeEach
    void setUp() {
        RedisTemplate<String, Object> redisTemplate = createInMemoryRedisTemplate();
        repository = new RecommendationCacheRepositoryImpl(redisTemplate);
        inMemoryStore.clear();
    }

    @Test
    void putGetHybridShouldWork() {
        Recommendation recommendation = buildRecommendation("isbn-1", 0.9, Recommendation.RecommendationType.HYBRID);

        repository.putHybridRecommendations(10L, 5, List.of(recommendation));
        List<Recommendation> result = repository.getHybridRecommendations(10L, 5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("isbn-1", result.get(0).getBook().getIsbn());
    }

    @Test
    void putGetPopularShouldWork() {
        Recommendation recommendation = buildRecommendation("isbn-2", 4.8, Recommendation.RecommendationType.CONTENT_BASED);

        repository.putPopularRecommendations(3, List.of(recommendation));
        List<Recommendation> result = repository.getPopularRecommendations(3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("isbn-2", result.get(0).getBook().getIsbn());
    }

    @Test
    void clearCachesShouldDeleteByPrefixes() {
        Recommendation hybrid = buildRecommendation("isbn-3", 0.7, Recommendation.RecommendationType.HYBRID);
        Recommendation popular = buildRecommendation("isbn-4", 4.6, Recommendation.RecommendationType.CONTENT_BASED);

        repository.putHybridRecommendations(11L, 5, List.of(hybrid));
        repository.putPopularRecommendations(5, List.of(popular));
        repository.clearRecommendationCaches();

        assertNull(repository.getHybridRecommendations(11L, 5));
        assertNull(repository.getPopularRecommendations(5));
    }

    private Recommendation buildRecommendation(String isbn, double score, Recommendation.RecommendationType type) {
        Book book = new Book();
        book.setIsbn(isbn);
        return new Recommendation(book, score, "reason", type);
    }

    @SuppressWarnings("unchecked")
    private RedisTemplate<String, Object> createInMemoryRedisTemplate() {
        ValueOperations<String, Object> valueOps = (ValueOperations<String, Object>) Proxy.newProxyInstance(
                ValueOperations.class.getClassLoader(),
                new Class<?>[]{ValueOperations.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("get".equals(methodName) && args != null && args.length == 1) {
                        return inMemoryStore.get(args[0]);
                    }
                    if ("set".equals(methodName) && args != null && (args.length == 2 || args.length == 3)) {
                        inMemoryStore.put(String.valueOf(args[0]), args[1]);
                        return null;
                    }
                    return null;
                }
        );

        return new RedisTemplate<>() {
            @Override
            public ValueOperations<String, Object> opsForValue() {
                return valueOps;
            }

            @Override
            public Set<String> keys(String pattern) {
                if (pattern == null || !pattern.endsWith("*")) {
                    return Set.of();
                }
                String prefix = pattern.substring(0, pattern.length() - 1);
                return inMemoryStore.keySet().stream()
                        .filter(key -> key.startsWith(prefix))
                        .collect(java.util.stream.Collectors.toSet());
            }

            @Override
            public Long delete(Collection<String> keys) {
                long deleted = 0;
                for (String key : keys) {
                    if (inMemoryStore.remove(key) != null) {
                        deleted++;
                    }
                }
                return deleted;
            }
        };
    }
}
