package cn.org.starpivot.api.system.dto;

import lombok.Data;

/**
 * 登录日志传输 DTO。
 * <p>
 * 用于 auth 服务通过 Feign 向 system 模块写入登录记录。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter/toString 等方法</li>
 * </ul>
 */
@Data
public class LoginLogDto {

    /** 登录用户名 */
    private String userName;

    /** 登录 IP 地址 */
    private String ipaddr;

    /** 登录地点（根据 IP 解析） */
    private String loginLocation;

    /** 浏览器类型 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 登录状态：0 成功，1 失败 */
    private String status;

    /** 提示消息（如"登录成功"、"密码错误"） */
    private String msg;
}
