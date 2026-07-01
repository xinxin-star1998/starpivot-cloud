package cn.org.starpivot.mall.portal.domain.vo;

import cn.org.starpivot.mall.oms.domain.vo.OmsOrderItemVo;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class PortalOrderVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 状态
     */
    private Integer status;

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
     * receiver名称
     */
    private String receiverName;

    /**
     * receiver Phone
     */
    private String receiverPhone;

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
     * delivery Company
     */
    private String deliveryCompany;

    /**
     * delivery Sn
     */
    private String deliverySn;

    /**
     * 创建时间
     */
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * order Item List
     */
    private List<OmsOrderItemVo> orderItemList;
}
