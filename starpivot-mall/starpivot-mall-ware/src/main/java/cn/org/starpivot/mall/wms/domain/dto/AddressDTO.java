package cn.org.starpivot.mall.wms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 地址DTO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class AddressDTO {

    /** 修改时必填 */
    /**
     * 主键 ID
     */
    private Long id;

    /**
     * code
     */
    /**
     * 优惠码
     */
    @NotBlank(message = "地区编码不能为空")
    @Size(max = 32, message = "地区编码长度不能超过32")
    /**
     * 优惠码
     */
    private String code;

    /**
     * parent Code
     */
    /**
     * parent Code
     */
    @Size(max = 32, message = "父级编码长度不能超过32")
    /**
     * parent Code
     */
    private String parentCode;

    /**
     * 名称
     */
    /**
     * name
     */
    @NotBlank(message = "地区名称不能为空")
    @Size(max = 255, message = "地区名称长度不能超过255")
    /**
     * name
     */
    private String name;

    /**
     * level
     */
    /**
     * level
     */
    @NotNull(message = "地区层级不能为空")
    /**
     * level
     */
    private Long level;
}
