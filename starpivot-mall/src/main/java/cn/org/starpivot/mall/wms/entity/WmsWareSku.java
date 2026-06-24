package cn.org.starpivot.mall.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("wms_ware_sku")
public class WmsWareSku {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private Long wareId;

    private Integer stock;

    private String skuName;

    private Integer stockLocked;

}
