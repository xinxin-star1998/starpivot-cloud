package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PortalCollectSubjectAddBo {

    @NotNull(message = "专题ID不能为空")
    private Long subjectId;
}
