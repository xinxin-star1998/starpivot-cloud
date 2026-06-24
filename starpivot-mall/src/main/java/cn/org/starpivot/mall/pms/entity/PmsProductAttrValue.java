package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_product_attr_value")
public class PmsProductAttrValue {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spuId;

    private Long attrId;

    private String attrName;

    private String attrValue;

    private Integer attrSort;

    private Integer quickShow;

}
