package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * C 端砍价助力请求 BO。
 */
@Data
public class PortalBargainHelpBo {

    @NotNull(message = "砍价记录不能为空")
    private Long recordId;
}
