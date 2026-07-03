package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.domain.vo.CommentReplyVo;
import cn.org.starpivot.mall.pms.entity.PmsCommentReplay;
import cn.org.starpivot.mall.pms.entity.PmsSpuComment;
import cn.org.starpivot.mall.pms.mapper.PmsCommentReplayMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuCommentMapper;
import cn.org.starpivot.mall.pms.service.PmsCommentReplayService;
import cn.org.starpivot.mall.portal.domain.vo.PortalCommentReplyVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PmsCommentReplayServiceImpl implements PmsCommentReplayService {

    private static final int COMMENT_TYPE_REVIEW = 0;
    private static final int COMMENT_TYPE_REPLY = 1;
    private static final int SHOW_STATUS_ON = 1;

    private final PmsSpuCommentMapper pmsSpuCommentMapper;
    private final PmsCommentReplayMapper pmsCommentReplayMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminReply(Long parentCommentId, String content) {
        if (!StringUtils.hasText(content)) {
            throw new BizException("回复内容不能为空");
        }
        PmsSpuComment parent = pmsSpuCommentMapper.selectById(parentCommentId);
        if (parent == null) {
            throw new BizException("评论不存在");
        }
        if (!Integer.valueOf(COMMENT_TYPE_REVIEW).equals(parent.getCommentType())) {
            throw new BizException("仅可对商品评价进行回复");
        }

        String operator = SecurityContextUtils.getUsername();
        PmsSpuComment reply = new PmsSpuComment();
        reply.setSpuId(parent.getSpuId());
        reply.setSkuId(parent.getSkuId());
        reply.setSpuName(parent.getSpuName());
        reply.setMemberNickName(StringUtils.hasText(operator) ? operator : "官方客服");
        reply.setContent(content.trim());
        reply.setCommentType(COMMENT_TYPE_REPLY);
        reply.setShowStatus(SHOW_STATUS_ON);
        reply.setLikesCount(0);
        reply.setReplyCount(0);
        reply.setCreateTime(LocalDateTime.now());
        pmsSpuCommentMapper.insert(reply);

        PmsCommentReplay relation = new PmsCommentReplay();
        relation.setCommentId(parentCommentId);
        relation.setReplyId(reply.getId());
        pmsCommentReplayMapper.insert(relation);

        int replyCount = parent.getReplyCount() != null ? parent.getReplyCount() : 0;
        parent.setReplyCount(replyCount + 1);
        pmsSpuCommentMapper.updateById(parent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentReplyVo> listByCommentId(Long commentId) {
        return toCommentReplyVos(loadVisibleReplies(commentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<PortalCommentReplyVo>> mapPortalRepliesByCommentIds(List<Long> commentIds) {
        if (CollectionUtils.isEmpty(commentIds)) {
            return Map.of();
        }
        List<PmsCommentReplay> relations = pmsCommentReplayMapper.selectList(
                Wrappers.<PmsCommentReplay>lambdaQuery().in(PmsCommentReplay::getCommentId, commentIds));
        if (relations.isEmpty()) {
            return Map.of();
        }
        List<Long> replyIds = relations.stream()
                .map(PmsCommentReplay::getReplyId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (replyIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, PmsSpuComment> replyMap = pmsSpuCommentMapper.selectBatchIds(replyIds).stream()
                .filter(reply -> reply.getId() != null && Integer.valueOf(SHOW_STATUS_ON).equals(reply.getShowStatus()))
                .collect(Collectors.toMap(PmsSpuComment::getId, reply -> reply, (a, b) -> a));

        Map<Long, List<PortalCommentReplyVo>> grouped = new LinkedHashMap<>();
        for (PmsCommentReplay relation : relations) {
            PmsSpuComment reply = replyMap.get(relation.getReplyId());
            if (reply == null || relation.getCommentId() == null) {
                continue;
            }
            grouped.computeIfAbsent(relation.getCommentId(), key -> new ArrayList<>())
                    .add(toPortalReplyVo(reply));
        }
        return grouped;
    }

    private List<PmsSpuComment> loadVisibleReplies(Long commentId) {
        if (commentId == null) {
            return List.of();
        }
        List<PmsCommentReplay> relations = pmsCommentReplayMapper.selectList(
                Wrappers.<PmsCommentReplay>lambdaQuery().eq(PmsCommentReplay::getCommentId, commentId));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> replyIds = relations.stream()
                .map(PmsCommentReplay::getReplyId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (replyIds.isEmpty()) {
            return List.of();
        }
        return pmsSpuCommentMapper.selectBatchIds(replyIds).stream()
                .filter(reply -> Integer.valueOf(SHOW_STATUS_ON).equals(reply.getShowStatus()))
                .sorted((a, b) -> {
                    if (a.getCreateTime() == null || b.getCreateTime() == null) {
                        return Long.compare(a.getId(), b.getId());
                    }
                    return a.getCreateTime().compareTo(b.getCreateTime());
                })
                .toList();
    }

    private List<CommentReplyVo> toCommentReplyVos(List<PmsSpuComment> replies) {
        List<CommentReplyVo> rows = new ArrayList<>(replies.size());
        for (PmsSpuComment reply : replies) {
            CommentReplyVo vo = new CommentReplyVo();
            vo.setId(reply.getId());
            vo.setMemberNickName(reply.getMemberNickName());
            vo.setContent(reply.getContent());
            vo.setCreateTime(reply.getCreateTime());
            if (StringUtils.hasText(reply.getMemberIcon())) {
                vo.setMemberIcon(StorageObjectPathUtils.normalizeToObjectName(reply.getMemberIcon()));
            }
            rows.add(vo);
        }
        return rows;
    }

    private PortalCommentReplyVo toPortalReplyVo(PmsSpuComment reply) {
        PortalCommentReplyVo vo = new PortalCommentReplyVo();
        BeanUtils.copyProperties(reply, vo);
        if (StringUtils.hasText(vo.getMemberIcon())) {
            vo.setMemberIcon(StorageObjectPathUtils.normalizeToObjectName(vo.getMemberIcon()));
        }
        return vo;
    }
}
