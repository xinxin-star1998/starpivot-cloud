package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_category")
public class PmsCategory {

    @TableId(value = "cat_id", type = IdType.AUTO)
    private Long catId;

    private String name;

    private Long parentCid;

    private Long catLevel;

    private Long showStatus;

    private Long sort;

    private String icon;

    private String productUnit;

    private Long productCount;

}
