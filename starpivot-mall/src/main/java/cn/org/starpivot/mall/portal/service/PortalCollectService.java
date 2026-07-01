package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.common.domain.PageReqBo;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.domain.bo.PortalCollectAddBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCollectSubjectAddBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCollectVo;

public interface PortalCollectService {

    PageResponse<PortalCollectVo> pageList(Long memberId, PageReqBo pageReq);

    void add(Long memberId, PortalCollectAddBo bo);

    void remove(Long memberId, Long spuId);

    boolean isCollected(Long memberId, Long spuId);

    void addSubject(Long memberId, PortalCollectSubjectAddBo bo);

    void removeSubject(Long memberId, Long subjectId);

    boolean isSubjectCollected(Long memberId, Long subjectId);
}
