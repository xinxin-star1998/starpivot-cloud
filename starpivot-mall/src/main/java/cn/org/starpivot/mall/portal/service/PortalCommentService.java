package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.common.domain.PageReqBo;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.domain.bo.PortalCommentQueryBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCommentSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCommentSummaryVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCommentVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalPendingReviewVo;

import java.util.List;

public interface PortalCommentService {

    PageResponse<PortalCommentVo> pageBySpu(PortalCommentQueryBo bo);

    PageResponse<PortalCommentVo> pageMine(Long memberId, PageReqBo pageReq);

    List<Long> filterReviewableSpuIds(Long memberId, List<Long> spuIds);

    List<PortalPendingReviewVo> listPendingReviews(Long memberId);

    int countPendingReviews(Long memberId);

    PortalCommentSummaryVo getSummary(Long spuId);

    void submit(Long memberId, PortalCommentSubmitBo bo);

    boolean canComment(Long memberId, Long spuId);
}
