package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.domain.bo.CommentReqBo;
import cn.org.starpivot.mall.pms.domain.vo.CommentVo;

import java.util.List;

public interface PmsSpuCommentService {

    PageResponse<CommentVo> pageList(CommentReqBo reqBo);

    CommentVo getById(Long id);

    void updateShowStatus(Long id, Integer showStatus);

    void removeByIds(List<Long> ids);
}
