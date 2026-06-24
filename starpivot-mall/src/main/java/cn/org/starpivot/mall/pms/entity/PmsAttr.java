package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_attr")
public class PmsAttr {

    @TableId(value = "attr_id", type = IdType.AUTO)
    private Long attrId;

    private String attrName;

    private Integer searchType;

    private Integer valueType;

    private String icon;

    private String valueSelect;

    private Integer attrType;

    private Long enable;

    private Long catelogId;

    private Integer showDesc;

}
