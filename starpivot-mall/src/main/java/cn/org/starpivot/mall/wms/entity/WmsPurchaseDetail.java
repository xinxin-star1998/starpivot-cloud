package cn.org.starpivot.mall.wms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wms_purchase_detail")
public class WmsPurchaseDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long purchaseId;

    private Long skuId;

    private Integer skuNum;

    private BigDecimal skuPrice;

    private Long wareId;

    private Integer status;

}
