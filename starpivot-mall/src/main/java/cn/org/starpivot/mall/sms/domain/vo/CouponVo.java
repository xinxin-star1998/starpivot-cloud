package cn.org.starpivot.mall.sms.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 优惠券视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CouponVo {

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
    private String couponImg;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    /**
     * end时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    /**
     * 类型
     */
    private Integer useType;
    /**
     * note
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime enableStartTime;
    /**
     * enableEnd时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime enableEndTime;
    /**
     * code
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
    /**
     * 审批实例 ID
     */
    private Long approvalInstanceId;
    /**
     * 审批状态
     */
    private String auditStatus;
    /**
     * spu List
     */
    private List<CouponSpuVo> spuList;
    /**
     * category List
     */
    private List<CouponCategoryVo> categoryList;
}
