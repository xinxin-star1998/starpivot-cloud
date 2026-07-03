package cn.org.starpivot.mall.pms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品分类DTO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PmsCategoryDTO {

    /**
     * Cat ID
     */
    private Long catId;

    /**
     * 名称
     */
    /**
     * name
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    /**
     * name
     */
    private String name;

    /**
     * ParentCid
     */
    private Long parentCid;

    /**
     * cat Level
     */
    private Integer catLevel;

    /**
     * 状态
     */
    private Integer showStatus;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * icon
     */
    private String icon;

    /**
     * product Unit
     */
    private String productUnit;

    /**
     * product数量
     */
    private Integer productCount;
}
