package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PortalMemberProfileBo {

    @Size(max = 64, message = "昵称最多64个字符")
    private String nickname;

    @Size(max = 500, message = "头像地址过长")
    private String header;

    private Integer gender;

    @Size(max = 200, message = "个性签名最多200个字符")
    private String sign;
}
