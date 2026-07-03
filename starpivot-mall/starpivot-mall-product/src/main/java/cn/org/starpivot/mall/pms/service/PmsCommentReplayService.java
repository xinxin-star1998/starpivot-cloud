package cn.org.starpivot.mall.pms.service;

import cn.org.starpivot.mall.pms.domain.vo.CommentReplyVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCommentReplyVo;

import java.util.List;
import java.util.Map;

/**
 * 商品评论回复服务。
 */
public interface PmsCommentReplayService {

    void adminReply(Long parentCommentId, String content);

    List<CommentReplyVo> listByCommentId(Long commentId);

    Map<Long, List<PortalCommentReplyVo>> mapPortalRepliesByCommentIds(List<Long> commentIds);
}
