package cn.org.starpivot.mall.oms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 退货视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class ReturnVo {

    /**
     * 主键 ID
     */
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
