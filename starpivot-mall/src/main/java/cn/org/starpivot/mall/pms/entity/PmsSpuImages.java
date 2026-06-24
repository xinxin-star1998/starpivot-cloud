package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_spu_images")
public class PmsSpuImages {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spuId;

    private String imgName;

    private String imgUrl;

    private Integer imgSort;

    private Integer defaultImg;

}
