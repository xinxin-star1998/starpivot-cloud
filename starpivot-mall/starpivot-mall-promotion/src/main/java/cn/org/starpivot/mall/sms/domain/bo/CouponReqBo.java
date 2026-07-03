package cn.org.starpivot.mall.sms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * 优惠券查询请求 BO。
 * <p>
 * 用于分页查询或列表筛选的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link EqualsAndHashCode} — 继承父类字段参与 equals/hashCode</li>
 * </ul>
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class CouponReqBo extends PageReqBo {

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 发布状态：0 未发布，1 已发布
     */
    private Integer publish;

    /**
     * 适用范围：0 全场，1 分类，2 商品
     */
    private Integer useType;

    /**
     * 有效期筛选起始（与 endTime 区间重叠）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validityStart;

    /**
     * 有效期筛选结束（与 startTime 区间重叠）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validityEnd;
}
