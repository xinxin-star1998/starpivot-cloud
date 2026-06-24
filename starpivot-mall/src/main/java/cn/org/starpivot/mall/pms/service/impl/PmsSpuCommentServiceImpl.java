package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.domain.bo.CommentReqBo;
import cn.org.starpivot.mall.pms.domain.vo.CommentVo;
import cn.org.starpivot.mall.pms.entity.PmsCommentReplay;
import cn.org.starpivot.mall.pms.entity.PmsSpuComment;
import cn.org.starpivot.mall.pms.mapper.PmsCommentReplayMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuCommentMapper;
import cn.org.starpivot.mall.pms.service.PmsSpuCommentService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PmsSpuCommentServiceImpl implements PmsSpuCommentService {

    private final PmsSpuCommentMapper pmsSpuCommentMapper;
    private final PmsCommentReplayMapper pmsCommentReplayMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentVo> pageList(CommentReqBo reqBo) {
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
        return toVo(comment);
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
