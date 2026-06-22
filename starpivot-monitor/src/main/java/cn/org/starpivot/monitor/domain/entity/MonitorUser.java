package cn.org.starpivot.monitor.domain.entity;

import lombok.Data;

/**
 * 监控模块使用的用户精简实体，映射 {@code sys_user} 表关键字段。
 * <p>
 * {@link Data}：生成 getter/setter 及 equals/hashCode。
 */
@Data
public class MonitorUser {

    /** 用户 ID */
    private Long userId;

    /** 登录账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 所属部门 ID */
    private Long deptId;
}
