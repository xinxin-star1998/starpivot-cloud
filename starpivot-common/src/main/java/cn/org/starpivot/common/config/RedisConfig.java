package cn.org.starpivot.common.config;

import cn.org.starpivot.common.cache.CacheConstants;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 序列化与 Spring Cache 配置类。
 * <p>
 * 统一 Key 为字符串、Value 为 JSON（含类型信息），并提供与 {@link CacheConstants} 对齐的
 * {@link RedisCacheManager}，供 {@code @Cacheable} 等注解使用。
 * <ul>
 *   <li>{@link Configuration} — 声明 Redis 相关 {@link Bean}，由 Spring 容器加载</li>
 * </ul>
 *
 * @see CacheConstants
 */
@Configuration
public class RedisConfig {

    /**
     * 创建 Redis 专用 {@link ObjectMapper}。
     * <p>
     * 启用受信任包范围内的多态类型信息，避免 {@code LaissezFaireSubTypeValidator} 带来的反序列化风险。
     */
    private static ObjectMapper createRedisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        BasicPolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("cn.org.starpivot")
                .allowIfSubType("java.util")
                .allowIfSubType("java.lang")
                .allowIfSubType("java.time")
                .allowIfSubType("java.math")
                .build();
        mapper.activateDefaultTyping(validator, ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }

    /**
     * 注册通用 {@link RedisTemplate}，Key/HashKey 使用字符串序列化，Value 使用 JSON。
     *
     * @param factory Redis 连接工厂（由 Spring Boot 自动配置提供）
     * @return 已完成序列化器配置的 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer strSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());

        template.setKeySerializer(strSerializer);
        template.setHashKeySerializer(strSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 注册适配 {@code @Cacheable} 的 {@link RedisCacheManager}。
     * <p>
     * 默认 TTL 取自 {@link CacheConstants#TTL_USER_PERMISSIONS}；
     * 各缓存名通过 {@link CacheConstants#springCacheTtls()} 覆盖独立 TTL。
     * Redis 键形如 {@code sys_dict::status}，且不缓存 null 值。
     *
     * @param factory Redis 连接工厂
     * @return 含默认与按名 TTL 配置的缓存管理器
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory) {
        StringRedisSerializer strSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(CacheConstants.TTL_USER_PERMISSIONS)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(strSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> customTtlMap = new HashMap<>();
        CacheConstants.springCacheTtls().forEach((name, ttl) ->
                customTtlMap.put(name, defaultConfig.entryTtl(ttl)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(customTtlMap)
                .build();
    }
}
