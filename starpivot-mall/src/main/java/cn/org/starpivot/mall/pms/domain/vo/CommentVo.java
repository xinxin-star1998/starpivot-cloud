package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 商品评论 VO（pms_spu_comment） */
@Data
public class CommentVo {

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
