package cn.org.starpivot.mall.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退货申请实体。
 * <p>
 * 对应数据库表 {@code oms_order_return_apply}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("oms_order_return_apply")
public class OmsOrderReturnApply {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Order ID
     */
    private Long orderId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * memberUsername
     */
    private String memberUsername;

    /**
     * 金额
     */
    private BigDecimal returnAmount;

    /**
     * return名称
     */
    private String returnName;

    /**
     * return Phone
     */
    private String returnPhone;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 审批实例 ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态：DRAFT/PENDING/APPROVED/REJECTED/WITHDRAWN
     */
    private String auditStatus;

    /**
     * handle时间
     */
    private LocalDateTime handleTime;

    /**
     * 图片
     */
    private String skuImg;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * sku Brand
     */
    private String skuBrand;

    /**
     * sku Attrs Vals
     */
    private String skuAttrsVals;

    /**
     * sku数量
     */
    private Integer skuCount;

    /**
     * sku Price
     */
    private BigDecimal skuPrice;

    /**
     * sku Real Price
     */
    private BigDecimal skuRealPrice;

    /**
     * reason
     */
    private String reason;

    /**
     * description
     */
    private String description;

    /**
     * desc Pics
     */
    private String descPics;

    /**
     * handle Note
     */
    private String handleNote;

    /**
     * handle Man
     */
    private String handleMan;

    /**
     * receive Man
     */
    private String receiveMan;

    /**
     * receive时间
     */
    private LocalDateTime receiveTime;

    /**
     * receive Note
     */
    private String receiveNote;

    /**
     * receive Phone
     */
    private String receivePhone;

    /**
     * company Address
     */
    private String companyAddress;

}
