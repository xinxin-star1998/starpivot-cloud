package cn.org.starpivot.auth.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 当前用户信息响应 DTO。
 * <p>
 * 封装 {@link cn.org.starpivot.auth.controller.AuthController#userInfo} 返回的用户详情、角色列表及菜单权限，
 * 字段结构与前端 star-pivot-ui 对齐。
 * </p>
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter、{@code equals}、{@code hashCode}、{@code toString}</li>
 *   <li>{@link Builder} — Lombok 生成建造者模式，便于 {@link cn.org.starpivot.auth.service.AuthService} 组装响应</li>
 * </ul>
 */
@Data
@Builder
public class UserInfoResponse {

    /** 用户基本信息 */
    private UserVo user;

    /** 用户所属角色列表 */
    private List<RoleVo> roles;

    /** 用户菜单权限列表（来自 system 服务的菜单树） */
    private List<Object> permissions;

    /**
     * 用户基本信息视图对象。
     * <ul>
     *   <li>{@link Data} — Lombok 自动生成 getter/setter 等样板方法</li>
     *   <li>{@link Builder} — Lombok 生成建造者模式</li>
     * </ul>
     */
    @Data
    @Builder
    public static class UserVo {

        /** 用户 ID */
        private Long userId;

        /** 登录用户名 */
        private String username;

        /** 用户昵称 */
        private String nickName;

        /** 头像 URL */
        private String avatar;

        /** 电子邮箱 */
        private String email;

        /** 手机号码 */
        private String phoneNumber;

        /** 性别（0 未知，1 男，2 女） */
        private Integer sex;

        /** 账号状态（0 正常，1 停用） */
        private String status;

        /** 账号创建时间 */
        private String createTime;
    }

    /**
     * 角色信息视图对象。
     * <ul>
     *   <li>{@link Data} — Lombok 自动生成 getter/setter 等样板方法</li>
     *   <li>{@link Builder} — Lombok 生成建造者模式</li>
     * </ul>
     */
    @Data
    @Builder
    public static class RoleVo {

        /** 角色 ID */
        private Long roleId;

        /** 角色名称 */
        private String roleName;

        /** 角色权限标识 */
        private String roleKey;

        /** 角色显示顺序 */
        private Integer roleSort;

        /** 角色状态（0 正常，1 停用） */
        private String status;

        /** 角色创建时间 */
        private String createTime;
    }
}
