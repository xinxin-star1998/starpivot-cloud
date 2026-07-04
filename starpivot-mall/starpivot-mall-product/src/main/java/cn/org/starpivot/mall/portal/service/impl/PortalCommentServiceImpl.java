package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.api.mall.order.dto.PendingReviewItemDto;
import cn.org.starpivot.api.member.dto.MemberDto;
import cn.org.starpivot.common.domain.PageReqBo;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.common.MemberFeignSupport;
import cn.org.starpivot.mall.common.OrderFeignSupport;
import cn.org.starpivot.mall.pms.domain.bo.CommentReqBo;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSpuComment;
import cn.org.starpivot.mall.pms.entity.PmsSpuImages;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuCommentMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuImagesMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.pms.service.PmsCommentReplayService;
import cn.org.starpivot.mall.portal.domain.bo.PortalCommentQueryBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCommentSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCommentReplyVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCommentSummaryVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCommentVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalPendingReviewVo;
import cn.org.starpivot.mall.portal.service.PortalCommentService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalCommentServiceImpl implements PortalCommentService {

    private static final int COMMENT_SHOW = 1;
    private static final int COMMENT_TYPE_REVIEW = 0;

    private final PmsSpuCommentMapper commentMapper;
    private final PmsCommentReplayService pmsCommentReplayService;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuImagesMapper pmsSpuImagesMapper;
    private final OrderFeignSupport orderFeignSupport;
    private final MemberFeignSupport memberFeignSupport;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PortalCommentVo> pageMine(Long memberId, PageReqBo pageReq) {
        CommentReqBo reqBo = new CommentReqBo();
        reqBo.setPageNum(pageReq.getPageNum());
        reqBo.setPageSize(pageReq.getPageSize());
        reqBo.setMemberId(memberId);
        reqBo.setCommentType(COMMENT_TYPE_REVIEW);

        Page<PmsSpuComment> page = new Page<>(pageReq.getPageNum(), pageReq.getPageSize());
        var pageList = commentMapper.selectPageList(page, reqBo);

        PageResponse<PortalCommentVo> response = new PageResponse<>();
        response.setTotal(pageList.getTotal());
        List<PortalCommentVo> rows = pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        enrichReplies(rows);
        response.setRows(rows);
        response.setPageNum(Long.valueOf(pageReq.getPageNum()));
        response.setPageSize(Long.valueOf(pageReq.getPageSize()));
        response.setPageCount((long) pageList.getPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PortalCommentVo> pageBySpu(PortalCommentQueryBo bo) {
        CommentReqBo reqBo = new CommentReqBo();
        reqBo.setPageNum(bo.getPageNum());
        reqBo.setPageSize(bo.getPageSize());
        reqBo.setSpuId(bo.getSpuId());
        reqBo.setShowStatus(COMMENT_SHOW);
        reqBo.setCommentType(COMMENT_TYPE_REVIEW);

        Page<PmsSpuComment> page = new Page<>(bo.getPageNum(), bo.getPageSize());
        var pageList = commentMapper.selectPageList(page, reqBo);

        PageResponse<PortalCommentVo> response = new PageResponse<>();
        response.setTotal(pageList.getTotal());
        List<PortalCommentVo> rows = pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        enrichReplies(rows);
        response.setRows(rows);
        response.setPageNum(bo.getPageNum());
        response.setPageSize(bo.getPageSize());
        response.setPageCount((long) pageList.getPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> filterReviewableSpuIds(Long memberId, List<Long> spuIds) {
        if (memberId == null || spuIds == null || spuIds.isEmpty()) {
            return List.of();
        }
        return spuIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(spuId -> canComment(memberId, spuId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortalPendingReviewVo> listPendingReviews(Long memberId) {
        if (memberId == null) {
            return List.of();
        }
        List<PortalPendingReviewVo> result = new ArrayList<>();
        for (PendingReviewItemDto item : orderFeignSupport.listReviewablePurchaseItems(memberId)) {
            Long spuId = item.getSpuId();
            if (spuId == null || !canComment(memberId, spuId)) {
                continue;
            }
            PortalPendingReviewVo vo = new PortalPendingReviewVo();
            vo.setSpuId(spuId);
            vo.setSkuId(item.getSkuId());
            vo.setSpuName(StringUtils.hasText(item.getSpuName()) ? item.getSpuName() : loadSpuName(spuId));
            vo.setCoverImg(StringUtils.hasText(item.getCoverImg()) ? item.getCoverImg() : resolveCover(spuId, null));
            vo.setOrderSn(item.getOrderSn());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public int countPendingReviews(Long memberId) {
        return listPendingReviews(memberId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public PortalCommentSummaryVo getSummary(Long spuId) {
        PortalCommentSummaryVo vo = new PortalCommentSummaryVo();
        vo.setSpuId(spuId);
        if (spuId == null) {
            vo.setTotal(0L);
            vo.setAvgStar(BigDecimal.ZERO);
            return vo;
        }
        Map<String, Object> row = commentMapper.selectSummaryBySpuId(spuId);
        long total = row != null && row.get("total") != null ? ((Number) row.get("total")).longValue() : 0L;
        vo.setTotal(total);
        if (total <= 0) {
            vo.setAvgStar(BigDecimal.ZERO);
            return vo;
        }
        Object avg = row.get("avgStar");
        if (avg instanceof BigDecimal decimal) {
            vo.setAvgStar(decimal.setScale(1, RoundingMode.HALF_UP));
        } else if (avg instanceof Number number) {
            vo.setAvgStar(BigDecimal.valueOf(number.doubleValue()).setScale(1, RoundingMode.HALF_UP));
        } else {
            vo.setAvgStar(BigDecimal.ZERO);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long memberId, PortalCommentSubmitBo bo) {
        if (!canComment(memberId, bo.getSpuId())) {
            throw new BizException("仅购买并已收货的商品可评价，或您已评价过该商品");
        }

        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(bo.getSpuId());
        if (spu == null) {
            throw new BizException("商品不存在");
        }
        PmsSkuInfo sku = pmsSkuInfoMapper.selectById(bo.getSkuId());
        if (sku == null || !bo.getSpuId().equals(sku.getSpuId())) {
            throw new BizException("SKU 与商品不匹配");
        }

        MemberDto member = memberFeignSupport.requireMember(memberId);

        PmsSpuComment comment = new PmsSpuComment();
        comment.setSpuId(bo.getSpuId());
        comment.setMemberId(memberId);
        comment.setSkuId(bo.getSkuId());
        comment.setSpuName(spu.getSpuName());
        comment.setMemberNickName(StringUtils.hasText(member.getNickname()) ? member.getNickname() : member.getUsername());
        comment.setMemberIcon(member.getHeader());
        comment.setStar(bo.getStar());
        comment.setContent(bo.getContent().trim());
        comment.setResources(bo.getResources());
        comment.setSpuAttributes(buildSkuAttrLabel(sku));
        comment.setShowStatus(COMMENT_SHOW);
        comment.setLikesCount(0);
        comment.setReplyCount(0);
        comment.setCommentType(0);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canComment(Long memberId, Long spuId) {
        if (memberId == null || spuId == null) {
            return false;
        }
        Long commented = commentMapper.selectCount(
                Wrappers.<PmsSpuComment>lambdaQuery()
                        .eq(PmsSpuComment::getMemberId, memberId)
                        .eq(PmsSpuComment::getSpuId, spuId));
        if (commented != null && commented > 0) {
            return false;
        }
        return hasPurchased(memberId, spuId);
    }

    private boolean hasPurchased(Long memberId, Long spuId) {
        return orderFeignSupport.hasPurchasedSpu(memberId, spuId);
    }

    private String buildSkuAttrLabel(PmsSkuInfo sku) {
        if (StringUtils.hasText(sku.getSkuTitle())) {
            return sku.getSkuTitle();
        }
        return sku.getSkuName();
    }

    private String loadSpuName(Long spuId) {
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(spuId);
        return spu != null ? spu.getSpuName() : null;
    }

    private String resolveCover(Long spuId, String fallbackPic) {
        if (StringUtils.hasText(fallbackPic)) {
            return StorageObjectPathUtils.normalizeToObjectName(fallbackPic.trim());
        }
        PmsSpuImages img = pmsSpuImagesMapper.selectOne(
                Wrappers.<PmsSpuImages>lambdaQuery()
                        .eq(PmsSpuImages::getSpuId, spuId)
                        .orderByDesc(PmsSpuImages::getDefaultImg)
                        .orderByAsc(PmsSpuImages::getImgSort)
                        .last("LIMIT 1"));
        if (img == null || !StringUtils.hasText(img.getImgUrl())) {
            return null;
        }
        return StorageObjectPathUtils.normalizeToObjectName(img.getImgUrl().trim());
    }

    private PortalCommentVo toVo(PmsSpuComment entity) {
        PortalCommentVo vo = new PortalCommentVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private void enrichReplies(List<PortalCommentVo> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        List<Long> commentIds = comments.stream()
                .map(PortalCommentVo::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, List<PortalCommentReplyVo>> replyMap =
                pmsCommentReplayService.mapPortalRepliesByCommentIds(commentIds);
        for (PortalCommentVo comment : comments) {
            if (comment.getId() == null) {
                continue;
            }
            comment.setReplies(replyMap.getOrDefault(comment.getId(), List.of()));
        }
    }
}
