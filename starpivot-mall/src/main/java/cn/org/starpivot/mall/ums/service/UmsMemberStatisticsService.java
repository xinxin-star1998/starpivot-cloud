package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberStatisticsReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberStatisticsVo;

public interface UmsMemberStatisticsService {

    PageResponse<MemberStatisticsVo> pageList(MemberStatisticsReqBo reqBo);

    MemberStatisticsVo getByMemberId(Long memberId);

    void refresh(Long memberId);
}
