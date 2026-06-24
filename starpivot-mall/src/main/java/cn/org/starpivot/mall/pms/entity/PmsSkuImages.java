package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_sku_images")
public class PmsSkuImages {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private String imgUrl;

    private Integer imgSort;

    private Integer defaultImg;

}
