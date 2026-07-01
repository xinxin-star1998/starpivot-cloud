package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PortalCommentVo {

    private Long id;

    private Long spuId;

    private Long skuId;

    private String memberNickName;

    private String memberIcon;

    private Integer star;

    private String content;

    private String spuName;

    private String resources;

    private String spuAttributes;

    private Integer likesCount;

    private Integer replyCount;

    private LocalDateTime createTime;

    private List<PortalCommentReplyVo> replies = new ArrayList<>();
}