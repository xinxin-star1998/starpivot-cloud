package cn.org.starpivot.mall.ums.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberReqBo extends PageReqBo {

    private String username;
    private String mobile;
    private Integer status;
}
