package cn.org.starpivot.mall.oms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("oms_order_item")
public class OmsOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String orderSn;

    private Long spuId;

    private String spuName;

    private String spuPic;

    private String spuBrand;

    private Long categoryId;

    private Long skuId;

    private String skuName;

    private String skuPic;

    private BigDecimal skuPrice;

    private Integer skuQuantity;

    private String skuAttrsVals;

    private BigDecimal promotionAmount;

    private BigDecimal couponAmount;

    private BigDecimal integrationAmount;

    private BigDecimal realAmount;

    private Integer giftIntegration;

    private Integer giftGrowth;

}
