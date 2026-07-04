package cn.org.starpivot.mall.ums.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员统计信息实体。
 * <p>
 * 对应数据库表 {@code ums_member_statistics_info}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("ums_member_statistics_info")
public class UmsMemberStatisticsInfo {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员 ID
     */
    private Long memberId;

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
