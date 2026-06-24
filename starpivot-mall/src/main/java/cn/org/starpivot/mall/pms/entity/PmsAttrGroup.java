package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_attr_group")
public class PmsAttrGroup {

    @TableId(value = "attr_group_id", type = IdType.AUTO)
    private Long attrGroupId;

    private String attrGroupName;

    private Integer sort;

    private String descript;

    private String icon;

    private Long catelogId;

}
