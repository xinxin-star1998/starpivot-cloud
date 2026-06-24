package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 评论展示状态 */
@Data
public class CommentShowStatusBo {

    @NotNull(message = "评论 ID 不能为空")
    private Long id;

    /** 0-隐藏 1-显示 */
    @NotNull(message = "展示状态不能为空")
    private Integer showStatus;
}
