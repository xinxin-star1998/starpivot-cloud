package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.vo.PortalSubjectDetailVo;

/**
 * C 端专题活动服务。
 */
public interface PortalSubjectService {

    /**
     * 专题详情（含分页商品列表，仅上架商品）。
     */
    PortalSubjectDetailVo getDetail(Long subjectId, int pageNum, int pageSize);
}
