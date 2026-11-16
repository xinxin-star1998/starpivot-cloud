package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalBargainHelpBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalBargainOrderBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalBargainStartBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainActivityVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalBargainRecordVo;

import java.util.List;

/**
 * C 端砍价服务。
 */
public interface PortalBargainService {

    /**
     * 在售砍价活动列表。
     *
     * @return 活动列表
     */
    List<PortalBargainActivityVo> listActivities();

    /**
     * 砍价活动详情（含当前会员的砍价记录）。
     *
     * @param memberId   会员 ID，未登录可为空
     * @param activityId 活动 ID
     * @return 活动详情
     */
    PortalBargainDetailVo detail(Long memberId, Long activityId);

    /**
     * 按记录 ID 查看砍价详情（分享给好友助力场景，无需登录）。
     *
     * @param recordId 砍价记录 ID
     * @return 砍价记录
     */
    PortalBargainRecordVo recordDetail(Long recordId);

    /**
     * 发起砍价。
     *
     * @param memberId 会员 ID
     * @param bo       请求参数
     * @return 砍价记录
     */
    PortalBargainRecordVo start(Long memberId, PortalBargainStartBo bo);

    /**
     * 好友助力砍价。
     *
     * @param memberId 助力会员 ID
     * @param bo       请求参数
     * @return 砍价记录
     */
    PortalBargainRecordVo help(Long memberId, PortalBargainHelpBo bo);

    /**
     * 砍到底价后下单。
     *
     * @param memberId 会员 ID
     * @param bo       下单参数
     * @return 砍价记录
     */
    PortalBargainRecordVo order(Long memberId, PortalBargainOrderBo bo);

    /**
     * 我的砍价记录。
     *
     * @param memberId 会员 ID
     * @return 记录列表
     */
    List<PortalBargainRecordVo> myBargains(Long memberId);

    /**
     * 订单支付成功回调。
     *
     * @param orderId 订单 ID
     */
    void onOrderPaid(Long orderId);

    /**
     * 失效超时未使用的砍价记录。
     *
     * @return 处理条数
     */
    int settleExpired();
}