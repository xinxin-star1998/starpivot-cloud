package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类排序项请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CategorySortItemBo {

    /**
     * Cat ID
     */
    /**
     * cat ID
     */
    @NotNull(message = "分类ID不能为空")
    /**
     * cat ID
     */
    private Long catId;

    /**
     * 排序
     */
    /**
     * sort
     */
    @NotNull(message = "排序值不能为空")
    /**
     * sort
     */
    private Integer sort;
}
