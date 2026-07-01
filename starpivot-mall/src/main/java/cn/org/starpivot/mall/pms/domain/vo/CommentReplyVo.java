package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentReplyVo {

    private Long id;

    private Long commentId;

    private String memberNickName;

    private String memberIcon;

    private String content;

    private LocalDateTime createTime;
}
