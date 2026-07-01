package cn.org.starpivot.mall.sms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HomeCategoryHotReqBo extends PageReqBo {

    private String title;

    private Integer status;

    private Long catId;
}
