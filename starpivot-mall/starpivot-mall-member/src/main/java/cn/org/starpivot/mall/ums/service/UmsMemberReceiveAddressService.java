package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberReceiveAddressReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberReceiveAddressVo;

/**
 * 会员收货地址 B 端查询服务。
 */
public interface UmsMemberReceiveAddressService {

    PageResponse<MemberReceiveAddressVo> pageList(MemberReceiveAddressReqBo reqBo);
}
