package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_brand")
public class PmsBrand {

    @TableId(value = "brand_id", type = IdType.AUTO)
    private Long brandId;

    private String name;

    private String logo;

    private String descript;

    private Integer showStatus;

    private String firstLetter;

    private Integer sort;

}
