package cn.org.starpivot.mall.ums.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ums_member_collect_spu")
public class UmsMemberCollectSpu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private Long spuId;

    private String spuName;

    private String spuImg;

    private LocalDateTime createTime;

}
