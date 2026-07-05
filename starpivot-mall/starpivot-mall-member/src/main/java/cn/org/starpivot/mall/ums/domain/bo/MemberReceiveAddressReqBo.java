package cn.org.starpivot.mall.ums.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员收货地址 B 端查询 BO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberReceiveAddressReqBo extends PageReqBo {

    private Long memberId;

    private String name;

    private String phone;

    private String province;

    private Integer defaultStatus;
}
