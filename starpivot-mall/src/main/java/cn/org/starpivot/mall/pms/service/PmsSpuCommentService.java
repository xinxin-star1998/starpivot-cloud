package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.pms.domain.bo.CommentReplyBo;
import cn.org.starpivot.mall.pms.domain.bo.CommentReqBo;
import cn.org.starpivot.mall.pms.domain.vo.CommentReplyVo;
import cn.org.starpivot.mall.pms.domain.vo.CommentVo;

import java.util.List;

/**
 * Spucommentservice服务接口。
 * <p>
 * 封装商品评论相关业务逻辑。
 * </p>
 */

public interface PmsSpuCommentService {

    /**
     * 分页查询列表。
     */
    PageResponse<CommentVo> pageList(CommentReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    CommentVo getById(Long id);

    /**
     * 修改记录。
     */
    void updateShowStatus(Long id, Integer showStatus);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);

    void reply(CommentReplyBo bo);

    List<CommentReplyVo> listReplies(Long commentId);
}
