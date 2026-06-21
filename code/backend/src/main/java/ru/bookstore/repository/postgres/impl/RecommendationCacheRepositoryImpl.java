package ru.bookstore.repository.postgres.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.bookstore.domain.Recommendation;
import ru.bookstore.repository.RecommendationCacheRepository;

import java.util.ArrayList;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@RequiredArgsConstructor
public class RecommendationCacheRepositoryImpl implements RecommendationCacheRepository {

    private static final String HYBRID_PREFIX = "rec:hybrid:";
    private static final String POPULAR_PREFIX = "rec:popular:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, List<Recommendation>> fallbackCache = new ConcurrentHashMap<>();

    @Override
    public List<Recommendation> getHybridRecommendations(Long userId, int count) {
        String key = hybridKey(userId, count);
        List<Recommendation> cached = readFromRedis(key);
        if (cached != null) {
            return cached;
        }
        return copy(fallbackCache.get(key));
    }

    @Override
    public void putHybridRecommendations(Long userId, int count, List<Recommendation> recommendations) {
        String key = hybridKey(userId, count);
        List<Recommendation> value = copy(recommendations);
        fallbackCache.put(key, value);
        writeToRedis(key, value);
    }

    @Override
    public List<Recommendation> getPopularRecommendations(int count) {
        String key = popularKey(count);
        List<Recommendation> cached = readFromRedis(key);
        if (cached != null) {
            return cached;
        }
        return copy(fallbackCache.get(key));
    }

    @Override
    public void putPopularRecommendations(int count, List<Recommendation> recommendations) {
        String key = popularKey(count);
        List<Recommendation> value = copy(recommendations);
        fallbackCache.put(key, value);
        writeToRedis(key, value);
    }

    @Override
    public void clearRecommendationCaches() {
        fallbackCache.clear();
        try {
            if (redisTemplate == null) {
                return;
            }
            Set<String> keys = new HashSet<>();
            Set<String> hybridKeys = redisTemplate.keys(HYBRID_PREFIX + "*");
            if (hybridKeys != null) {
                keys.addAll(hybridKeys);
            }
            Set<String> popularKeys = redisTemplate.keys(POPULAR_PREFIX + "*");
            if (popularKeys != null) {
                keys.addAll(popularKeys);
            }
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (RuntimeException ignored) {
        }
    }

    private String hybridKey(Long userId, int count) {
        return HYBRID_PREFIX + userId + ":" + count;
    }

    private String popularKey(int count) {
        return POPULAR_PREFIX + count;
    }

    @SuppressWarnings("unchecked")
    private List<Recommendation> castRecommendations(Object cached) {
        if (cached instanceof List<?>) {
            return copy((List<Recommendation>) cached);
        }
        return null;
    }

    private List<Recommendation> readFromRedis(String key) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            return castRecommendations(cached);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void writeToRedis(String key, List<Recommendation> recommendations) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, recommendations, DEFAULT_TTL);
        } catch (RuntimeException ignored) {
        }
    }

    private List<Recommendation> copy(List<Recommendation> value) {
        if (value == null) {
            return null;
        }
        return new ArrayList<>(value);
    }
}
