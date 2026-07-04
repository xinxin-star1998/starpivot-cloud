package cn.org.starpivot.mall.ums.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员统计视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class MemberStatisticsVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 会员 ID
     */
    private Long memberId;
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
     * 金额
     */
    private BigDecimal consumeAmount;
    /**
     * 金额
     */
    private BigDecimal couponAmount;
    /**
     * order数量
     */
    private Integer orderCount;
    /**
     * coupon数量
     */
    private Integer couponCount;
    /**
     * comment数量
     */
    private Integer commentCount;
    /**
     * returnOrder数量
     */
    private Integer returnOrderCount;
    /**
     * login数量
     */
    private Integer loginCount;
    /**
     * attend数量
     */
    private Integer attendCount;
    /**
     * fans数量
     */
    private Integer fansCount;
    /**
     * collectProduct数量
     */
    private Integer collectProductCount;
    /**
     * collectSubject数量
     */
    private Integer collectSubjectCount;
    /**
     * collectComment数量
     */
    private Integer collectCommentCount;
    /**
     * inviteFriend数量
     */
    private Integer inviteFriendCount;
}
