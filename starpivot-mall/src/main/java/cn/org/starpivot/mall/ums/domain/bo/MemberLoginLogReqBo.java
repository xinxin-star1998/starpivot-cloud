package cn.org.starpivot.mall.ums.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员登录日志查询 BO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberLoginLogReqBo extends PageReqBo {

    private Long memberId;

    private Integer loginType;

    private String ip;
}
