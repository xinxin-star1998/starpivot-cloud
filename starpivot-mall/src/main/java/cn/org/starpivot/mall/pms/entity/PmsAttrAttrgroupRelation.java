package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_attr_attrgroup_relation")
public class PmsAttrAttrgroupRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long attrId;

    private Long attrGroupId;

    private Integer attrSort;

}
