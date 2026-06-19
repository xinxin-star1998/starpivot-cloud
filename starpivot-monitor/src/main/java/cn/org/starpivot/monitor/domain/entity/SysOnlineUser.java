package cn.org.starpivot.monitor.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户记录实体类（历史记录）
 * <p>
 * 说明：用于存储在线用户的历史记录，实时数据存储在 Redis 中。
 * 当用户登出、强制下线或会话过期时，将记录保存到此表。
 * </p>
 *
 * @author xinxin
 * @since 2026-01-25
 */
@Data
@TableName("sys_online_user")
public class SysOnlineUser {

    /**
     * 会话ID（Redis key，格式：jwt:refresh:user:{userId}）
     */
    @TableId("session_id")
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 状态（0在线 1离线）
     */
    private String status;

    /**
     * 会话开始时间（登录时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTimestamp;

    /**
     * 最后访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastAccessTime;

    /**
     * 会话结束时间（登出/强制下线时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTimestamp;

    /**
     * 会话超时时间（秒）
     */
    private Integer expireTime;

    /**
     * 令牌标识
     */
    private String tokenId;

    /**
     * 下线类型（0正常登出 1强制下线 2过期下线）
     */
    private String logoutType;

    /**
     * 创建时间（记录入库时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
