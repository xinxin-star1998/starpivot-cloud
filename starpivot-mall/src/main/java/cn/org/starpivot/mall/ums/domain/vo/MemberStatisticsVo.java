package cn.org.starpivot.mall.ums.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class MemberStatisticsVo {

    private Long id;
    private Long memberId;
    private String username;
    private String nickname;
    private String mobile;
    private BigDecimal consumeAmount;
    private BigDecimal couponAmount;
    private Integer orderCount;
    private Integer couponCount;
    private Integer commentCount;
    private Integer returnOrderCount;
    private Integer loginCount;
    private Integer attendCount;
    private Integer fansCount;
    private Integer collectProductCount;
    private Integer collectSubjectCount;
    private Integer collectCommentCount;
    private Integer inviteFriendCount;
}
