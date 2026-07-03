package cn.org.starpivot.mall.portal.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * C 端会员登录方式绑定。
 */
@Data
@TableName("ums_member_auth")
public class UmsMemberAuth {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    /** 1密码 2手机 3微信 ... */
    private Integer authType;

    private String identifier;

    private String credential;

    private String extraJson;

    private LocalDateTime bindTime;

    private LocalDateTime lastLogin;

    /** 1正常 0已解绑 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
