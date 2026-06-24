package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_category_brand_relation")
public class PmsCategoryBrandRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long brandId;

    private Long catelogId;

    private String brandName;

    private String catelogName;

}
