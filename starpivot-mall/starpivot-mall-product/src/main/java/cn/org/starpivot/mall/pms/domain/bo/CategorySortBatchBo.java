package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 分类批量排序请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CategorySortBatchBo {

    /**
     * items
     */
    /**
     * items
     */
    @NotEmpty(message = "排序项不能为空")
    @Valid
    /**
     * items
     */
    private List<CategorySortItemBo> items;
}
