package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 品牌保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class BrandSaveBo {

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
    @Size(max = 128, message = "品牌名称长度不能超过128")
    /**
     * name
     */
    private String name;

    /**
     * logo
     */
    /**
     * logo
     */
    @Size(max = 512, message = "Logo 长度不能超过512")
    /**
     * logo
     */
    private String logo;

    /**
     * descript
     */
    private String descript;
    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    /**
     * 状态
     */
    @NotNull(message = "显示状态不能为空")
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
    @Size(max = 1, message = "首字母为单字符")
    /**
     * first Letter
     */
    private String firstLetter;
}
