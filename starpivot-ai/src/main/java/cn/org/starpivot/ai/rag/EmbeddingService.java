package cn.org.starpivot.ai.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Service
public class EmbeddingService {

    private static final String CACHE_PREFIX = "ai:emb:v1:";
    private static final int BATCH_SIZE = 20;

    private final EmbeddingModel embeddingModel;
    private final StringRedisTemplate redisTemplate;
    private final EmbeddingService self;

    @Value("${starpivot.ai.rag.embedding-cache-ttl:7d}")
    private Duration embeddingTtl;

    @Autowired
    public EmbeddingService(@Autowired(required = false) EmbeddingModel embeddingModel,
                            StringRedisTemplate redisTemplate,
                            @Lazy EmbeddingService self) {
        this.embeddingModel = embeddingModel;
        this.redisTemplate = redisTemplate;
        this.self = self;
    }

    public boolean isAvailable() {
        return embeddingModel != null;
    }

    public List<float[]> embedBatch(List<String> texts) {
        if (!isAvailable() || texts == null || texts.isEmpty()) {
            return List.of();
        }

        Map<Integer, float[]> cached = new HashMap<>();
        List<Integer> missedIndices = new ArrayList<>();
        List<String> missedTexts = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            String cacheKey = buildCacheKey(texts.get(i));
            String cachedStr = redisTemplate.opsForValue().get(cacheKey);
            if (cachedStr != null) {
                cached.put(i, deserializeVector(cachedStr));
            } else {
                missedIndices.add(i);
                missedTexts.add(texts.get(i));
            }
        }

        if (!missedTexts.isEmpty()) {
            List<float[]> newVectors = self.embedFromApi(missedTexts);
            for (int j = 0; j < missedIndices.size(); j++) {
                int originalIndex = missedIndices.get(j);
                float[] vector = newVectors.get(j);
                cached.put(originalIndex, vector);
                redisTemplate.opsForValue().set(
                        buildCacheKey(texts.get(originalIndex)),
                        serializeVector(vector),
                        embeddingTtl);
            }
        }

        return IntStream.range(0, texts.size()).mapToObj(cached::get).toList();
    }

    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            noRetryFor = HttpClientErrorException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<float[]> embedFromApi(List<String> texts) {
        List<float[]> result = new ArrayList<>();
        for (int start = 0; start < texts.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, texts.size());
            List<String> batch = texts.subList(start, end);
            EmbeddingResponse response = embeddingModel.call(new EmbeddingRequest(batch, null));
            response.getResults().stream()
                    .sorted(Comparator.comparingInt(r -> r.getIndex()))
                    .forEach(r -> result.add(r.getOutput()));
        }
        return result;
    }

    @Recover
    public List<float[]> embedFromApiFallback(Exception ex, List<String> texts) {
        throw new RuntimeException("Embedding API 调用失败：" + ex.getMessage(), ex);
    }

    public float[] embed(String text) {
        List<float[]> result = embedBatch(List.of(text));
        return result.isEmpty() ? new float[0] : result.get(0);
    }

    private String buildCacheKey(String text) {
        return CACHE_PREFIX + toMd5(text);
    }

    private String toMd5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return String.valueOf(text.hashCode());
        }
    }

    private String serializeVector(float[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        return sb.toString();
    }

    private float[] deserializeVector(String str) {
        str = str.replace("[", "").replace("]", "").replace(" ", "");
        String[] parts = str.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i]);
        }
        return vector;
    }
}
