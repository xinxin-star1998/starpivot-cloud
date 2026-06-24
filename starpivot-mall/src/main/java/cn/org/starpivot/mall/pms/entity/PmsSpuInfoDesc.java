package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_spu_info_desc")
public class PmsSpuInfoDesc {

    @TableId(value = "spu_id", type = IdType.INPUT)
    private Long spuId;

    private String decript;

}
