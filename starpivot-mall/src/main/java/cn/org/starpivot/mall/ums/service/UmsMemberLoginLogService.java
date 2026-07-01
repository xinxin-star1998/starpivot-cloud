package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberLoginLogReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberLoginLogVo;

public interface UmsMemberLoginLogService {

    PageResponse<MemberLoginLogVo> pageList(MemberLoginLogReqBo reqBo);
}
