package cn.org.starpivot.mall.pms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class PmsBrandDTO {

    /**
     * 品牌 ID
     */
    private Long brandId;

    /**
     * 名称
     */
    /**
     * name
     */
    @NotBlank(message = "品牌名称不能为空")
    @Size(max = 50, message = "品牌名称长度不能超过50个字符")
    /**
     * name
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
    /**
     * first Letter
     */
    @Size(max = 1, message = "检索首字母长度不能超过1个字符")
    /**
     * first Letter
     */
    private String firstLetter;

    /**
     * 排序
     */
    private Integer sort;
}
