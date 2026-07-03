package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

/**
 * 品牌分类视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class BrandCategoryVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 分类 ID
     */
    private Long catelogId;
    /**
     * catelog名称
     */
    private String catelogName;
}
