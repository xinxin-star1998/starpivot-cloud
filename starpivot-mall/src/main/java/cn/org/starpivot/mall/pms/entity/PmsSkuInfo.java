package cn.org.starpivot.mall.pms.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_sku_info")
public class PmsSkuInfo {

    @TableId(value = "sku_id", type = IdType.AUTO)
    private Long skuId;

    private Long spuId;

    private String skuName;

    private String skuDesc;

    private Long catalogId;

    private Long brandId;

    private String skuDefaultImg;

    private String skuTitle;

    private String skuSubtitle;

    private BigDecimal price;

    private Long saleCount;

}
