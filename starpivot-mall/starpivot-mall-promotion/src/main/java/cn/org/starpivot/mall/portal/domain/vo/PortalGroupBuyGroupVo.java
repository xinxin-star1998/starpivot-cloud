package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * C 端进行中的拼团团信息。
 */
@Data
public class PortalGroupBuyGroupVo {

    private String groupNo;

    /** 团长会员 ID */
    private Long leaderMemberId;

    /** 已参团人数 */
    private Integer joinedCount;

    private Integer groupSize;

    /** 还差几人成团 */
    private Integer remainCount;

    private LocalDateTime expireTime;

    /** 剩余秒数，便于前端倒计时 */
    private Long remainSeconds;
}
