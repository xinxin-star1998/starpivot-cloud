package cn.org.starpivot.mall.pms.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_spu_comment")
public class PmsSpuComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long skuId;

    private Long spuId;

    private String spuName;

    private String memberNickName;

    private Integer star;

    private String memberIp;

    private LocalDateTime createTime;

    private Integer showStatus;

    private String spuAttributes;

    private Integer likesCount;

    private Integer replyCount;

    private String resources;

    private String content;

    private String memberIcon;

    private Integer commentType;

}
