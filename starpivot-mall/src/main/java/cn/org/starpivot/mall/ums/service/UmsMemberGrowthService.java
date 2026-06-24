package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberGrowthReqBo;
import cn.org.starpivot.mall.ums.domain.vo.GrowthChangeHistoryVo;
import cn.org.starpivot.mall.ums.domain.vo.IntegrationChangeHistoryVo;

public interface UmsMemberGrowthService {

    PageResponse<IntegrationChangeHistoryVo> integrationPageList(MemberGrowthReqBo reqBo);

    PageResponse<GrowthChangeHistoryVo> growthPageList(MemberGrowthReqBo reqBo);
}
