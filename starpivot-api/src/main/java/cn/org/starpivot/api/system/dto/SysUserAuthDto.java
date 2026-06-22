package cn.org.starpivot.api.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户鉴权信息 DTO（数据传输对象）。
 * <p>
 * 封装登录鉴权所需的用户信息，不含密码字段；
 * 密码校验须通过 {@code verify-password} 内部接口完成。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter 等方法</li>
 *   <li>{@link Builder} — 支持建造者模式构建实例</li>
 *   <li>{@link NoArgsConstructor} / {@link AllArgsConstructor} — 无参/全参构造器</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserAuthDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 用户昵称 */
    private String nickName;
    /** 账号状态（0 正常 1 停用） */
    private String status;
    /** 角色标识列表 */
    private List<String> roles;
    /** 头像地址 */
    private String avatar;
}
