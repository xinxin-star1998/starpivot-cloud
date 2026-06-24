package cn.org.starpivot.mall.ums.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ums_member_level")
public class UmsMemberLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer growthPoint;

    private Integer defaultStatus;

    private BigDecimal freeFreightPoint;

    private Integer commentGrowthPoint;

    private Integer privilegeFreeFreight;

    private Integer privilegeMemberPrice;

    private Integer privilegeBirthday;

    private String note;

}
