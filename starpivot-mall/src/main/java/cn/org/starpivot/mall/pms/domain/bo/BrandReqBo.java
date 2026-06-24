package cn.org.starpivot.mall.pms.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BrandReqBo extends PageReqBo {

    private String name;
    private Integer showStatus;
    private String firstLetter;
}
