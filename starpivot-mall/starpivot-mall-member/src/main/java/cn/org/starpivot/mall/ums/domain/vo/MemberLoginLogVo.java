package cn.org.starpivot.mall.ums.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员登录日志 VO。
 */
@Data
public class MemberLoginLogVo {

    private Long id;

    private Long memberId;

    private String memberUsername;

    private String memberNickname;

    private LocalDateTime createTime;

    private String ip;

    private String city;

    private Integer loginType;

    private String loginTypeLabel;
}
