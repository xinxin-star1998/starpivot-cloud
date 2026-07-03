package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PortalCommentReplyVo {

    private Long id;

    private String memberNickName;

    private String memberIcon;

    private String content;

    private LocalDateTime createTime;
}
