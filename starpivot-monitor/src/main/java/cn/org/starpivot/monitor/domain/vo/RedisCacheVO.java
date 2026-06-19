package cn.org.starpivot.monitor.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Redis 缓存管理 VO
 *
 * @author xinxin
 * @since 2026-01-25
 */
@Data
public class RedisCacheVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 键名列表
     */
    private List<CacheKeyInfo> keys;

    /**
     * 缓存键信息
     */
    @Data
    public static class CacheKeyInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 键名
         */
        private String key;

        /**
         * 键类型（string, hash, list, set, zset）
         */
        private String type;

        /**
         * 过期时间（秒），-1表示永不过期，-2表示键不存在
         */
        private Long ttl;

        /**
         * 值大小（字节）
         */
        private Long size;
    }

    /**
     * 缓存内容信息
     */
    @Data
    public static class CacheContentInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 缓存名称
         */
        private String cacheName;

        /**
         * 缓存键名
         */
        private String key;

        /**
         * 缓存内容（JSON 格式）
         */
        private String content;

        /**
         * 键类型
         */
        private String type;

        /**
         * 过期时间（秒）
         */
        private Long ttl;
    }
}
