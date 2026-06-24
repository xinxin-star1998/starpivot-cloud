package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberReqBo;
import cn.org.starpivot.mall.ums.domain.bo.MemberSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberVo;

public interface UmsMemberService {

    PageResponse<MemberVo> pageList(MemberReqBo reqBo);

    MemberVo getById(Long id);

    void update(MemberSaveBo bo);
}
