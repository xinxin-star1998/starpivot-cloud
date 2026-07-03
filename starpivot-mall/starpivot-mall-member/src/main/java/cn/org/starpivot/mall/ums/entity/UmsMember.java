package cn.org.starpivot.mall.ums.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会员实体。
 * <p>
 * 对应数据库表 {@code ums_member}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("ums_member")
public class UmsMember {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
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
     * 密码
     */
    private String password;

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
