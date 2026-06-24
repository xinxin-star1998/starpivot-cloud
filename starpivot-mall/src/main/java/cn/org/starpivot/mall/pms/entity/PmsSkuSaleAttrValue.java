package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_sku_sale_attr_value")
public class PmsSkuSaleAttrValue {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private Long attrId;

    private String attrName;

    private String attrValue;

    private Integer attrSort;

}
