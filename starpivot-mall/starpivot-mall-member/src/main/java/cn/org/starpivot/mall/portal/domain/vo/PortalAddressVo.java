package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

/**
 * 地址视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalAddressVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * post Code
     */
    private String postCode;

    /**
     * province
     */
    private String province;

    /**
     * city
     */
    private String city;

    /**
     * region
     */
    private String region;

    /**
     * detail Address
     */
    private String detailAddress;

    /**
     * 状态
     */
    private Integer defaultStatus;
}
