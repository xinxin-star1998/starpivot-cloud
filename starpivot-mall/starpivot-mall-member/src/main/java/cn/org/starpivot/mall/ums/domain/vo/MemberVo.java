package cn.org.starpivot.mall.ums.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 会员视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class MemberVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * Level ID
     */
    private Long levelId;
    /**
     * 用户名
     */
    private String username;
    /**
     * nickname
     */
    private String nickname;
    /**
     * mobile
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * header
     */
    private String header;
    /**
     * gender
     */
    private Integer gender;
    /**
     * birth
     */
    private LocalDate birth;
    /**
     * city
     */
    private String city;
    /**
     * job
     */
    private String job;
    /**
     * sign
     */
    private String sign;
    /**
     * 类型
     */
    private Integer sourceType;
    /**
     * integration
     */
    private Integer integration;
    /**
     * growth
     */
    private Integer growth;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * SocialUid
     */
    private String socialUid;
    /**
     * access Token
     */
    private String accessToken;
    /**
     * expires In
     */
    private String expiresIn;
}
