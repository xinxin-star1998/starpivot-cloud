package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论回复 BO（商家回复）。
 */
@Data
public class CommentReplyBo {

    @NotNull(message = "评论 ID 不能为空")
    private Long commentId;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}
