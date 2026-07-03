package cn.org.starpivot.mall.pms.domain.dto;

import lombok.Data;


/**
 * 品牌DTO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PmsBrandQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    /** 品牌名称（模糊匹配） */
    /**
     * 名称
     */
    private String name;

    /** 显示状态 */
    /**
     * 状态
     */
    private Integer showStatus;

    /** 检索首字母 */
    /**
     * first Letter
     */
    private String firstLetter;
}
