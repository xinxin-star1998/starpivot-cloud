package cn.org.starpivot.mall.portal.auth.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PortalWechatLoginBo {

    @NotBlank(message = "code 不能为空")
    private String code;

    @NotBlank(message = "state 不能为空")
    private String state;
}
