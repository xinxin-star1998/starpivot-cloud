package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 地址保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalAddressSaveBo {

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
    @NotBlank(message = "收货人不能为空")
    /**
     * name
     */
    private String name;

    /**
     * 手机号
     */
    /**
     * phone
     */
    @NotBlank(message = "手机号不能为空")
    /**
     * phone
     */
    private String phone;

    /**
     * post Code
     */
    private String postCode;

    /**
     * province
     */
    /**
     * province
     */
    @NotBlank(message = "省份不能为空")
    /**
     * province
     */
    private String province;

    /**
     * city
     */
    /**
     * city
     */
    @NotBlank(message = "城市不能为空")
    /**
     * city
     */
    private String city;

    /**
     * region
     */
    /**
     * region
     */
    @NotBlank(message = "区/县不能为空")
    /**
     * region
     */
    private String region;

    /**
     * detail Address
     */
    /**
     * detail Address
     */
    @NotBlank(message = "详细地址不能为空")
    /**
     * detail Address
     */
    private String detailAddress;

    /** 1=默认地址 */
    /**
     * 状态
     */
    private Integer defaultStatus;
}
