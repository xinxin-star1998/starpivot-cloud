package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 仓库保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class WmsWareInfoSaveBo {

    /** 修改时必填 */
    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 名称
     */
    /**
     * name
     */
    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 128, message = "仓库名称长度不能超过128")
    /**
     * name
     */
    private String name;

    /**
     * address
     */
    /**
     * address
     */
    @Size(max = 512, message = "详细地址长度不能超过512")
    /**
     * address
     */
    private String address;

    /**
     * areacode
     */
    /**
     * areacode
     */
    @Size(max = 64, message = "区域编码长度不能超过64")
    /**
     * areacode
     */
    private String areacode;
}
