package cn.org.starpivot.mall.wms.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PurchaseDetailVo {

    private Long id;

    private Long purchaseId;

    private Long skuId;

    private Integer skuNum;

    private BigDecimal skuPrice;

    private Long wareId;

    private Integer status;
}
