package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * C 端发起砍价请求 BO。
 */
@Data
public class PortalBargainStartBo {

    @NotNull(message = "砍价活动不能为空")
    private Long activityId;
}
