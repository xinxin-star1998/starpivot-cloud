package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品分类保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CategorySaveBo {

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
    @Size(max = 128, message = "分类名称长度不能超过128")
    /**
     * name
     */
    private String name;

    /**
     * ParentCid
     */
    private Long parentCid;
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
     * icon
     */
    /**
     * 图标
     */
    @Size(max = 512, message = "图标长度不能超过512")
    /**
     * 图标
     */
    private String icon;

    /**
     * product Unit
     */
    /**
     * product Unit
     */
    @Size(max = 32, message = "计量单位长度不能超过32")
    /**
     * product Unit
     */
    private String productUnit;
}
