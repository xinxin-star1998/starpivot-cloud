package cn.org.starpivot.mall.pms.domain.bo;

import lombok.Data;


/**
 * 品牌视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PmsBrandVO {

    /**
     * 品牌 ID
     */
    private Long brandId;

    /**
     * 名称
     */
    private String name;

    /**
     * logo
     */
    private String logo;

    /**
     * descript
     */
    private String descript;

    /**
     * 状态
     */
    private Integer showStatus;

    /**
     * first Letter
     */
    private String firstLetter;

    /**
     * 排序
     */
    private Integer sort;
}
