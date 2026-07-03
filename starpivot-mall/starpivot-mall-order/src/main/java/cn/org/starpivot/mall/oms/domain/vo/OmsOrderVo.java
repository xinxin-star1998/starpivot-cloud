package cn.org.starpivot.mall.oms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 订单视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class OmsOrderVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 会员 ID
     */
    private Long memberId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 优惠券 ID
     */
    private Long couponId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * memberUsername
     */
    private String memberUsername;

    /**
     * 订单总额
     */
    private BigDecimal totalAmount;

    /**
     * 应付金额
     */
    private BigDecimal payAmount;

    /**
     * 金额
     */
    private BigDecimal freightAmount;

    /**
     * 金额
     */
    private BigDecimal promotionAmount;

    /**
     * 金额
     */
    private BigDecimal integrationAmount;

    /**
     * 金额
     */
    private BigDecimal couponAmount;

    /**
     * 金额
     */
    private BigDecimal discountAmount;

    /**
     * 类型
     */
    private Integer payType;

    /**
     * 类型
     */
    private Integer sourceType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * delivery Company
     */
    private String deliveryCompany;

    /**
     * delivery Sn
     */
    private String deliverySn;

    /**
     * auto Confirm Day
     */
    private Integer autoConfirmDay;

    /**
     * integration
     */
    private Integer integration;

    /**
     * growth
     */
    private Integer growth;

    /**
     * 类型
     */
    private Integer billType;

    /**
     * bill Header
     */
    private String billHeader;

    /**
     * bill Content
     */
    private String billContent;

    /**
     * bill Receiver Phone
     */
    private String billReceiverPhone;

    /**
     * bill Receiver Email
     */
    private String billReceiverEmail;

    /**
     * receiver名称
     */
    private String receiverName;

    /**
     * receiver Phone
     */
    private String receiverPhone;

    /**
     * receiver Post Code
     */
    private String receiverPostCode;

    /**
     * receiver Province
     */
    private String receiverProvince;

    /**
     * receiver City
     */
    private String receiverCity;

    /**
     * receiver Region
     */
    private String receiverRegion;

    /**
     * receiver Detail Address
     */
    private String receiverDetailAddress;

    /**
     * note
     */
    private String note;

    /**
     * 状态
     */
    private Integer confirmStatus;

    /**
     * 状态
     */
    private Integer deleteStatus;

    /**
     * use Integration
     */
    private Integer useIntegration;

    /**
     * payment时间
     */
    private LocalDateTime paymentTime;

    /**
     * delivery时间
     */
    private LocalDateTime deliveryTime;

    /**
     * receive时间
     */
    private LocalDateTime receiveTime;

    /**
     * comment时间
     */
    private LocalDateTime commentTime;

    /**
     * modify时间
     */
    private LocalDateTime modifyTime;

    /**
     * order Item List
     */
    private List<OmsOrderItemVo> orderItemList;
}
