package cn.org.starpivot.mall.ums.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberStatisticsReqBo extends PageReqBo {

    private Long memberId;
    private String username;
    private String mobile;
}
