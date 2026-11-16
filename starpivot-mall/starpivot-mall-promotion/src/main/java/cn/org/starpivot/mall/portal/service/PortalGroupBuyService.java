package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalGroupBuyJoinBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalGroupBuyStartBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyActivityVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalGroupBuyRecordVo;

import java.util.List;

/**
 * C 端拼团服务。
 */
public interface PortalGroupBuyService {

    /**
     * 在售拼团活动列表。
     *
     * @return 活动列表
     */
    List<PortalGroupBuyActivityVo> listActivities();

    /**
     * 拼团活动详情（含可参与的进行中团）。
     *
     * @param activityId 活动 ID
     * @return 活动详情
     */
    PortalGroupBuyDetailVo detail(Long activityId);

    /**
     * 开团：创建团长记录并提交拼团价订单。
     *
     * @param memberId 会员 ID
     * @param bo       开团参数
     * @return 参与记录
     */
    PortalGroupBuyRecordVo start(Long memberId, PortalGroupBuyStartBo bo);

    /**
     * 参团：加入指定进行中的团并提交订单。
     *
     * @param memberId 会员 ID
     * @param bo       参团参数
     * @return 参与记录
     */
    PortalGroupBuyRecordVo join(Long memberId, PortalGroupBuyJoinBo bo);

    /**
     * 我的拼团记录。
     *
     * @param memberId 会员 ID
     * @return 记录列表
     */
    List<PortalGroupBuyRecordVo> myGroups(Long memberId);

    /**
     * 订单支付成功回调：更新团状态并尝试成团。
     *
     * @param orderId 订单 ID
     */
    void onOrderPaid(Long orderId);

    /**
     * 结算超时未支付记录与超时未成团团。
     *
     * @return 处理条数
     */
    int settleExpired();
}
