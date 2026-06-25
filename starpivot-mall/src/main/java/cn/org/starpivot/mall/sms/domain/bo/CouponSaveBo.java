package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CouponSaveBo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 优惠券类型
     */
    private Integer couponType;

    /**
     * 图片
     */
    /**
     * coupon Img
     */
    @Size(max = 512, message = "优惠券图片长度不能超过512")
    /**
     * coupon Img
     */
    private String couponImg;

    /**
     * 优惠券名称
     */
    /**
     * 优惠券名称
     */
    @NotBlank(message = "优惠券名称不能为空")
    @Size(max = 128, message = "优惠券名称长度不能超过128")
    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * num
     */
    private Integer num;
    /**
     * 金额
     */
    private BigDecimal amount;
    /**
     * per Limit
     */
    private Integer perLimit;
    /**
     * min Point
     */
    private BigDecimal minPoint;
    /**
     * start时间
     */
    private LocalDateTime startTime;
    /**
     * end时间
     */
    private LocalDateTime endTime;
    /**
     * 类型
     */
    private Integer useType;

    /**
     * note
     */
    /**
     * 备注
     */
    @Size(max = 512, message = "备注长度不能超过512")
    /**
     * 备注
     */
    private String note;

    /**
     * publish数量
     */
    private Integer publishCount;
    /**
     * use数量
     */
    private Integer useCount;
    /**
     * receive数量
     */
    private Integer receiveCount;
    /**
     * enableStart时间
     */
    private LocalDateTime enableStartTime;
    /**
     * enableEnd时间
     */
    private LocalDateTime enableEndTime;

    /**
     * code
     */
    /**
     * 优惠码
     */
    @Size(max = 64, message = "优惠码长度不能超过64")
    /**
     * 优惠码
     */
    private String code;

    /**
     * member Level
     */
    private Integer memberLevel;
    /**
     * publish
     */
    private Integer publish;

    /** useType=2 时关联 SPU */
    /**
     * spu List
     */
    private List<CouponSpuBo> spuList;

    /** useType=1 时关联分类 */
    /**
     * category List
     */
    private List<CouponCategoryBo> categoryList;
}
