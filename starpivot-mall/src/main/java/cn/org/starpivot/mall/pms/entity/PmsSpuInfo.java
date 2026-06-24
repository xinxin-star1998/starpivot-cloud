package cn.org.starpivot.mall.pms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_spu_info")
public class PmsSpuInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String spuName;

    private String spuDescription;

    private Long catalogId;

    private Long brandId;

    private BigDecimal weight;

    private Integer publishStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
