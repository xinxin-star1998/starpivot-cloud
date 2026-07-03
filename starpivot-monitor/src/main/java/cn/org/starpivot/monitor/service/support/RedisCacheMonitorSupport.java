package cn.org.starpivot.monitor.service.support;

import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.monitor.domain.vo.RedisCacheVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.org.starpivot.common.util.LogUtils.truncateString;

/**
 * Redis 缓存监控与管理（SCAN 非阻塞扫描、内容查看、批量删除）。
 */
@Slf4j
@Component
public class RedisCacheMonitorSupport {

    private static final int SCAN_COUNT = 100;
    private static final int CACHE_CONTENT_MAX_LENGTH = 5000;
    private static final long REDIS_CAPABILITY_CACHE_MS = 60_000L;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private volatile ObjectMapper cacheViewObjectMapper;
    private volatile RedisCapabilities redisCapabilities;
    private final AtomicBoolean clusterScanWarningLogged = new AtomicBoolean(false);

    public List<RedisCacheVO> getCacheList() {
        requireRedis();
        try {
            Map<String, RedisCacheVO> resultMap = new LinkedHashMap<>();
            for (String groupName : CacheConstants.predefinedGroups()) {
                RedisCacheVO vo = new RedisCacheVO();
                vo.setCacheName(groupName);
                vo.setRemark(CacheConstants.getRemark(groupName));
                resultMap.put(groupName, vo);
            }

            Set<String> allKeys = scanKeys("*");
            for (String key : allKeys) {
                String groupName = extractCacheGroupName(key);
                if (groupName == null || groupName.isEmpty()) {
                    continue;
                }
                resultMap.computeIfAbsent(groupName, name -> {
                    RedisCacheVO vo = new RedisCacheVO();
                    vo.setCacheName(name);
                    vo.setRemark(CacheConstants.getRemark(name));
                    return vo;
                });
            }

            return new ArrayList<>(resultMap.values());
        } catch (Exception e) {
            log.error("获取缓存列表失败", e);
            throw new BizException(ErrorCode.REDIS_ERROR, "获取缓存列表失败: " + e.getMessage());
        }
    }

    public List<RedisCacheVO.CacheKeyInfo> getCacheKeys(String cacheName) {
        requireRedis();
        if (cacheName == null || cacheName.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "缓存名称不能为空");
        }

        try {
            String keyPattern = cacheName + ":*";
            Set<String> keys = scanKeys(keyPattern);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheName))) {
                keys.add(cacheName);
            }
            List<RedisCacheVO.CacheKeyInfo> keyInfoList = new ArrayList<>();

            for (String key : keys) {
                RedisCacheVO.CacheKeyInfo keyInfo = new RedisCacheVO.CacheKeyInfo();
                keyInfo.setKey(key);

                String type = redisTemplate.execute((RedisCallback<String>) connection -> {
                    DataType dataType = connection.keyCommands().type(key.getBytes());
                    return dataType != null ? dataType.code() : "unknown";
                });
                keyInfo.setType(type != null ? type : "unknown");

                Long ttl = redisTemplate.getExpire(key);
                keyInfo.setTtl(ttl != null ? ttl : -2L);

                try {
                    Object value = redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        keyInfo.setSize((long) value.toString().getBytes().length);
                    } else {
                        keyInfo.setSize(0L);
                    }
                } catch (Exception e) {
                    log.debug("获取键大小失败，key: {}", key, e);
                    keyInfo.setSize(0L);
                }

                keyInfoList.add(keyInfo);
            }

            return keyInfoList;
        } catch (Exception e) {
            log.error("获取缓存键列表失败，cacheName: {}", cacheName, e);
            throw new BizException(ErrorCode.REDIS_ERROR, "获取缓存键列表失败: " + e.getMessage());
        }
    }

    public RedisCacheVO.CacheContentInfo getCacheContent(String cacheName, String key) {
        requireRedis();
        if (key == null || key.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "缓存键名不能为空");
        }

        try {
            RedisCacheVO.CacheContentInfo contentInfo = new RedisCacheVO.CacheContentInfo();
            contentInfo.setCacheName(cacheName);
            contentInfo.setKey(key);

            Boolean exists = redisTemplate.hasKey(key);
            if (!Boolean.TRUE.equals(exists)) {
                contentInfo.setType("none");
                contentInfo.setTtl(-2L);
                contentInfo.setContent("(键不存在)");
                return contentInfo;
            }

            String type = redisTemplate.execute((RedisCallback<String>) connection -> {
                try {
                    DataType dataType = connection.keyCommands().type(key.getBytes());
                    return dataType != null ? dataType.code() : "unknown";
                } catch (Exception e) {
                    log.warn("获取键类型失败，key: {}", key, e);
                    return "unknown";
                }
            });
            contentInfo.setType(type != null ? type : "unknown");

            Long ttl = redisTemplate.getExpire(key);
            contentInfo.setTtl(ttl != null ? ttl : -2L);
            contentInfo.setContent(getCacheValueByType(key, type));
            return contentInfo;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取缓存内容失败，cacheName: {}, key: {}", cacheName, key, e);
            throw new BizException(ErrorCode.REDIS_ERROR, "获取缓存内容失败: " + e.getMessage());
        }
    }

    public long deleteCache(String cacheName) {
        requireRedis();
        if (cacheName == null || cacheName.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "缓存名称不能为空");
        }

        try {
            long total = 0L;
            Boolean deletedExact = redisTemplate.delete(cacheName);
            if (Boolean.TRUE.equals(deletedExact)) {
                total++;
            }

            String keyPattern = cacheName + ":*";
            Set<String> keys = scanKeys(keyPattern);
            if (!keys.isEmpty()) {
                Long deletedBatch = redisTemplate.delete(keys);
                total += deletedBatch != null ? deletedBatch : 0L;
            }

            return total;
        } catch (Exception e) {
            log.error("删除缓存失败，cacheName: {}", cacheName, e);
            throw new BizException(ErrorCode.REDIS_ERROR, "删除缓存失败: " + e.getMessage());
        }
    }

    public boolean deleteCacheKey(String cacheName, String key) {
        requireRedis();
        if (key == null || key.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "缓存键名不能为空");
        }

        try {
            Boolean deleted = redisTemplate.delete(key);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("删除缓存键失败，cacheName: {}, key: {}", cacheName, key, e);
            throw new BizException(ErrorCode.REDIS_ERROR, "删除缓存键失败: " + e.getMessage());
        }
    }

    public boolean clearAllCache() {
        requireRedis();
        try {
            RedisCapabilities capabilities = getRedisCapabilities();
            if (capabilities.supportAsyncFlush) {
                try {
                    redisTemplate.execute((RedisCallback<Object>) connection -> {
                        RedisServerCommands serverCommands = connection.serverCommands();
                        serverCommands.flushDb(RedisServerCommands.FlushOption.ASYNC);
                        return null;
                    });
                    log.info("Redis 当前数据库已成功清空（FLUSHDB ASYNC）");
                    return true;
                } catch (Exception asyncEx) {
                    log.warn("FLUSHDB ASYNC 执行失败，自动降级为同步 FLUSHDB，原因: {}", asyncEx.getMessage());
                    refreshRedisCapabilities(false);
                }
            }

            redisTemplate.execute((RedisCallback<Object>) connection -> {
                RedisServerCommands serverCommands = connection.serverCommands();
                serverCommands.flushDb();
                return null;
            });
            log.info("Redis 当前数据库已成功清空（FLUSHDB）");
            return true;
        } catch (Exception e) {
            log.error("清空所有缓存失败", e);
            throw new BizException(ErrorCode.REDIS_ERROR, "清空所有缓存失败: " + e.getMessage());
        }
    }

    private void requireRedis() {
        if (redisTemplate == null) {
            throw new BizException(ErrorCode.REDIS_ERROR, "Redis 未配置或未启用");
        }
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        if (redisTemplate == null) {
            return keys;
        }

        RedisCapabilities capabilities = getRedisCapabilities();
        if (capabilities.clusterMode && clusterScanWarningLogged.compareAndSet(false, true)) {
            log.warn("Redis 当前为集群模式，SCAN 结果可能为局部视图，pattern: {}", pattern);
        }

        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(SCAN_COUNT)
                    .build();

            try (Cursor<String> cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
            }
        } catch (Exception e) {
            log.error("使用 SCAN 命令扫描 key 失败，pattern: {}", pattern, e);
        }

        return keys;
    }

    private String extractCacheGroupName(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        int doubleColon = key.indexOf("::");
        if (doubleColon > 0) {
            return key.substring(0, doubleColon);
        }
        int colonIndex = key.indexOf(':');
        if (colonIndex > 0) {
            return key.substring(0, colonIndex);
        }
        return key;
    }

    private String getCacheValueByType(String key, String type) {
        if (type == null || "none".equals(type) || "unknown".equals(type)) {
            return "(无法获取内容)";
        }

        try {
            ObjectMapper objectMapper = getOrCreateCacheViewObjectMapper();

            switch (type.toLowerCase()) {
                case "string":
                    try {
                        Object stringValue = redisTemplate.opsForValue().get(key);
                        if (stringValue != null) {
                            return truncateString(formatObjectValue(stringValue, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                        }
                    } catch (Exception e) {
                        log.debug("反序列化失败，尝试获取原始字节，key: {}, error: {}", key, e.getMessage());
                    }
                    try {
                        byte[] rawBytes = redisTemplate.execute((RedisCallback<byte[]>) connection ->
                                connection.stringCommands().get(key.getBytes()));
                        if (rawBytes != null) {
                            String rawString = new String(rawBytes, "UTF-8");
                            try {
                                Object jsonValue = objectMapper.readValue(rawString, Object.class);
                                String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonValue);
                                return truncateString(pretty, CACHE_CONTENT_MAX_LENGTH);
                            } catch (Exception e) {
                                return truncateString(rawString, CACHE_CONTENT_MAX_LENGTH);
                            }
                        }
                    } catch (Exception e) {
                        log.debug("获取原始字节失败，key: {}", key, e);
                    }
                    return "(空值)";

                case "hash":
                    Map<Object, Object> hashValue = redisTemplate.opsForHash().entries(key);
                    if (!hashValue.isEmpty()) {
                        return truncateString(formatObjectValue(hashValue, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                    }
                    return "(空哈希)";

                case "list":
                    List<Object> listValue = redisTemplate.opsForList().range(key, 0, -1);
                    if (listValue != null && !listValue.isEmpty()) {
                        return truncateString(formatObjectValue(listValue, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                    }
                    return "(空列表)";

                case "set":
                    Set<Object> setValue = redisTemplate.opsForSet().members(key);
                    if (setValue != null && !setValue.isEmpty()) {
                        return truncateString(formatObjectValue(setValue, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                    }
                    return "(空集合)";

                case "zset":
                    Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> zsetWithScores =
                            redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
                    if (zsetWithScores != null && !zsetWithScores.isEmpty()) {
                        Map<String, Double> zsetMap = new LinkedHashMap<>();
                        for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object> tuple : zsetWithScores) {
                            Object member = tuple.getValue();
                            Double score = tuple.getScore();
                            if (member != null) {
                                zsetMap.put(member.toString(), score != null ? score : 0.0);
                            }
                        }
                        return truncateString(formatObjectValue(zsetMap, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                    }
                    return "(空有序集合)";

                default:
                    try {
                        Object value = redisTemplate.opsForValue().get(key);
                        if (value != null) {
                            return truncateString(formatObjectValue(value, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                        }
                    } catch (Exception e) {
                        log.debug("尝试获取未知类型键值失败，key: {}, type: {}", key, type, e);
                    }
                    return "(不支持的类型: " + type + ")";
            }
        } catch (Exception e) {
            log.error("获取缓存值失败，key: {}, type: {}", key, type, e);
            return "(获取内容失败: " + e.getMessage() + ")";
        }
    }

    private String formatObjectValue(Object value, ObjectMapper objectMapper) {
        if (value == null) {
            return "(null)";
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            log.debug("Jackson 序列化失败，尝试使用反射获取对象信息: {}", e.getMessage());
            return formatObjectWithReflection(value);
        } catch (Exception e) {
            log.debug("序列化失败，使用反射方式: {}", e.getMessage());
            return formatObjectWithReflection(value);
        }
    }

    private String formatObjectWithReflection(Object value) {
        if (value == null) {
            return "(null)";
        }

        try {
            Class<?> clazz = value.getClass();
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"@class\": \"").append(clazz.getName()).append("\",\n");

            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            boolean hasFields = false;
            for (java.lang.reflect.Field field : fields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                        "serialVersionUID".equals(field.getName())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(value);
                    if (hasFields) {
                        sb.append(",\n");
                    }
                    sb.append("  \"").append(field.getName()).append("\": ");

                    if (fieldValue == null) {
                        sb.append("null");
                    } else if (fieldValue instanceof String || fieldValue instanceof Number || fieldValue instanceof Boolean) {
                        if (fieldValue instanceof String) {
                            sb.append("\"").append(escapeJsonString(fieldValue.toString())).append("\"");
                        } else {
                            sb.append(fieldValue);
                        }
                    } else {
                        sb.append("\"").append(escapeJsonString(fieldValue.getClass().getSimpleName() + ": " + fieldValue)).append("\"");
                    }
                    hasFields = true;
                } catch (Exception e) {
                    log.debug("无法访问字段 {}: {}", field.getName(), e.getMessage());
                }
            }

            if (!hasFields) {
                sb.append("  \"toString\": \"").append(escapeJsonString(value.toString())).append("\"");
            }

            sb.append("\n}");
            return sb.toString();
        } catch (Exception e) {
            log.debug("反射格式化失败，使用 toString(): {}", e.getMessage());
            return "{\n  \"@class\": \"" + value.getClass().getName() + "\",\n" +
                    "  \"toString\": \"" + escapeJsonString(value.toString()) + "\"\n}";
        }
    }

    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private ObjectMapper getOrCreateCacheViewObjectMapper() {
        ObjectMapper local = cacheViewObjectMapper;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cacheViewObjectMapper == null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
                cacheViewObjectMapper = mapper;
            }
            return cacheViewObjectMapper;
        }
    }

    private RedisCapabilities getRedisCapabilities() {
        RedisCapabilities local = redisCapabilities;
        long now = System.currentTimeMillis();
        if (local != null && now - local.detectedAt <= REDIS_CAPABILITY_CACHE_MS) {
            return local;
        }
        synchronized (this) {
            local = redisCapabilities;
            now = System.currentTimeMillis();
            if (local != null && now - local.detectedAt <= REDIS_CAPABILITY_CACHE_MS) {
                return local;
            }
            RedisCapabilities detected = detectRedisCapabilities();
            redisCapabilities = detected;
            return detected;
        }
    }

    private void refreshRedisCapabilities(boolean supportAsyncFlush) {
        RedisCapabilities current = getRedisCapabilities();
        redisCapabilities = new RedisCapabilities(
                current.version,
                current.clusterMode,
                supportAsyncFlush,
                System.currentTimeMillis()
        );
    }

    private RedisCapabilities detectRedisCapabilities() {
        String version = "unknown";
        boolean clusterMode = false;

        try {
            Properties serverInfo = redisTemplate.execute((RedisCallback<Properties>) connection ->
                    connection.serverCommands().info("server"));
            if (serverInfo != null) {
                Object versionVal = serverInfo.get("redis_version");
                if (versionVal != null) {
                    version = String.valueOf(versionVal);
                }
            }
        } catch (Exception e) {
            log.debug("获取 Redis 版本信息失败，使用默认兼容策略", e);
        }

        try {
            Properties clusterInfo = redisTemplate.execute((RedisCallback<Properties>) connection ->
                    connection.serverCommands().info("cluster"));
            if (clusterInfo != null) {
                Object enabled = clusterInfo.get("cluster_enabled");
                clusterMode = "1".equals(String.valueOf(enabled));
            }
        } catch (Exception e) {
            log.debug("获取 Redis 集群信息失败，按非集群处理", e);
        }

        boolean supportAsyncFlush = isVersionAtLeast(version, 4);
        if ("unknown".equals(version)) {
            supportAsyncFlush = true;
        }

        log.info("Redis 能力探测完成，version={}, clusterMode={}, supportAsyncFlush={}",
                version, clusterMode, supportAsyncFlush);
        return new RedisCapabilities(version, clusterMode, supportAsyncFlush, System.currentTimeMillis());
    }

    private boolean isVersionAtLeast(String version, int major) {
        if (version == null || version.isBlank() || "unknown".equals(version)) {
            return false;
        }
        try {
            String[] parts = version.split("\\.");
            int majorVersion = Integer.parseInt(parts[0]);
            return majorVersion >= major;
        } catch (Exception e) {
            return false;
        }
    }

    private static class RedisCapabilities {
        private final String version;
        private final boolean clusterMode;
        private final boolean supportAsyncFlush;
        private final long detectedAt;

        private RedisCapabilities(String version, boolean clusterMode, boolean supportAsyncFlush, long detectedAt) {
            this.version = version;
            this.clusterMode = clusterMode;
            this.supportAsyncFlush = supportAsyncFlush;
            this.detectedAt = detectedAt;
        }
    }
}
