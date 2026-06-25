package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 评论展示状态 */

/**
 * 评论展示状态请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CommentShowStatusBo {

    /**
     * 主键 ID
     */
    /**
     * 主键 ID
     */
    @NotNull(message = "评论 ID 不能为空")
    /**
     * 主键 ID
     */
    private Long id;

    /** 0-隐藏 1-显示 */
    /**
     * 状态
     */
    /**
     * 状态
     */
    @NotNull(message = "展示状态不能为空")
    /**
     * 状态
     */
    private Integer showStatus;
}
