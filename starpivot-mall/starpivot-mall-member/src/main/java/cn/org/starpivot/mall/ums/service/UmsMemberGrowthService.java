package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberGrowthReqBo;
import cn.org.starpivot.mall.ums.domain.vo.GrowthChangeHistoryVo;
import cn.org.starpivot.mall.ums.domain.vo.IntegrationChangeHistoryVo;

/**
 * Membergrowthservice服务接口。
 * <p>
 * 封装会员成长值相关业务逻辑。
 * </p>
 */

public interface UmsMemberGrowthService {

    /**
     * integrationPageList。
     */
    PageResponse<IntegrationChangeHistoryVo> integrationPageList(MemberGrowthReqBo reqBo);

    /**
     * growthPageList。
     */
    PageResponse<GrowthChangeHistoryVo> growthPageList(MemberGrowthReqBo reqBo);
}
