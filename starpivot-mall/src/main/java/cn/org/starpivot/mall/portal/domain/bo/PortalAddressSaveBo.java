package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PortalAddressSaveBo {

    private Long id;

    @NotBlank(message = "收货人不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String postCode;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区/县不能为空")
    private String region;

    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    /** 1=默认地址 */
    private Integer defaultStatus;
}
