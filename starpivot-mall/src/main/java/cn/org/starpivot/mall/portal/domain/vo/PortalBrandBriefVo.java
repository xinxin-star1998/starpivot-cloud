package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

/**
 * Brandbrief视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalBrandBriefVo {

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
}
