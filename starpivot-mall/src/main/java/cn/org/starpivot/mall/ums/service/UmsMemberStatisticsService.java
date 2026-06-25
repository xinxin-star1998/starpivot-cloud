package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberStatisticsReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberStatisticsVo;

/**
 * Memberstatisticsservice服务接口。
 * <p>
 * 封装会员统计相关业务逻辑。
 * </p>
 */

public interface UmsMemberStatisticsService {

    /**
     * 分页查询列表。
     */
    PageResponse<MemberStatisticsVo> pageList(MemberStatisticsReqBo reqBo);

    /**
     * 获取ByMemberId。
     */
    MemberStatisticsVo getByMemberId(Long memberId);

    /**
     * refresh。
     */
    void refresh(Long memberId);
}
