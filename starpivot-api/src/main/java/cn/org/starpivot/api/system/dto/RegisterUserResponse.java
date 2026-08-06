package cn.org.starpivot.api.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部用户注册响应 DTO。
 * <p>
 * system 模块注册成功后返回的用户基本信息。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter 等方法</li>
 *   <li>{@link Builder} — Lombok 生成建造者模式，便于链式构建对象</li>
 *   <li>{@link NoArgsConstructor} / {@link AllArgsConstructor} — 显式生成无参和全参构造器，
 *       避免 {@link Builder} 生成的包级全参构造器覆盖默认无参构造器</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {

    /** 新创建的用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户昵称 */
    private String nickName;
}
