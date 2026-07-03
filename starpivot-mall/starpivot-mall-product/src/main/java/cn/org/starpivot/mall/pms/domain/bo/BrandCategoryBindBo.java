package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 品牌分类绑定请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class BrandCategoryBindBo {

    /**
     * 品牌 ID
     */
    /**
     * 品牌 ID
     */
    @NotNull(message = "品牌ID不能为空")
    /**
     * 品牌 ID
     */
    private Long brandId;

    /**
     * cat Ids
     */
    private List<Long> catIds;
}
