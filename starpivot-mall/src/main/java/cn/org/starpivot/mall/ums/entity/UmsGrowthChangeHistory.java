package cn.org.starpivot.mall.ums.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ums_growth_change_history")
public class UmsGrowthChangeHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private LocalDateTime createTime;

    private Integer changeCount;

    private String note;

    private Integer sourceType;

}
