package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.domain.bo.CommentReplyBo;
import cn.org.starpivot.mall.pms.domain.bo.CommentReqBo;
import cn.org.starpivot.mall.pms.domain.vo.CommentReplyVo;
import cn.org.starpivot.mall.pms.domain.vo.CommentVo;
import cn.org.starpivot.mall.pms.entity.PmsCommentReplay;
import cn.org.starpivot.mall.pms.entity.PmsSpuComment;
import cn.org.starpivot.mall.pms.mapper.PmsCommentReplayMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuCommentMapper;
import cn.org.starpivot.mall.pms.service.PmsCommentReplayService;
import cn.org.starpivot.mall.pms.service.PmsSpuCommentService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品评论服务实现类。
 * <p>
 * 实现 {@link PmsSpuCommentService}，处理商品评论相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see PmsSpuCommentService
 */

@Service
@RequiredArgsConstructor
public class PmsSpuCommentServiceImpl implements PmsSpuCommentService {

    private static final int COMMENT_TYPE_REVIEW = 0;

    private final PmsSpuCommentMapper pmsSpuCommentMapper;
    private final PmsCommentReplayMapper pmsCommentReplayMapper;
    private final PmsCommentReplayService pmsCommentReplayService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentVo> pageList(CommentReqBo reqBo) {
        if (reqBo.getCommentType() == null) {
            reqBo.setCommentType(COMMENT_TYPE_REVIEW);
        }
        Page<PmsSpuComment> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        IPage<PmsSpuComment> pageList = pmsSpuCommentMapper.selectPageList(page, reqBo);
        PageResponse<CommentVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public CommentVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "评论 ID 不能为空");
        }
        PmsSpuComment comment = pmsSpuCommentMapper.selectById(id);
        if (comment == null) {
            throw new BizException("评论不存在");
        }
        CommentVo vo = toVo(comment);
        vo.setReplies(pmsCommentReplayService.listByCommentId(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reply(CommentReplyBo bo) {
        pmsCommentReplayService.adminReply(bo.getCommentId(), bo.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentReplyVo> listReplies(Long commentId) {
        return pmsCommentReplayService.listByCommentId(commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShowStatus(Long id, Integer showStatus) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "评论 ID 不能为空");
        }
        if (showStatus == null || (showStatus != 0 && showStatus != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "展示状态仅支持 0（隐藏）或 1（显示）");
        }
        PmsSpuComment existing = pmsSpuCommentMapper.selectById(id);
        if (existing == null) {
            throw new BizException("评论不存在");
        }
        existing.setShowStatus(showStatus);
        pmsSpuCommentMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        List<Long> commentIds =
                ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (commentIds.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        pmsCommentReplayMapper.delete(
                Wrappers.<PmsCommentReplay>lambdaQuery().in(PmsCommentReplay::getCommentId, commentIds));
        pmsSpuCommentMapper.delete(Wrappers.<PmsSpuComment>lambdaQuery().in(PmsSpuComment::getId, commentIds));
    }

    private CommentVo toVo(PmsSpuComment comment) {
        CommentVo vo = new CommentVo();
        BeanUtils.copyProperties(comment, vo);
        if (StringUtils.hasText(vo.getMemberIcon())) {
            vo.setMemberIcon(StorageObjectPathUtils.normalizeToObjectName(vo.getMemberIcon()));
        }
        if (StringUtils.hasText(vo.getResources())) {
            vo.setResources(StorageObjectPathUtils.normalizeToObjectName(vo.getResources()));
        }
        return vo;
    }
}
