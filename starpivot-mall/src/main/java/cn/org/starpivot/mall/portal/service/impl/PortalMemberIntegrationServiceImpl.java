package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.portal.service.PortalMemberIntegrationService;
import cn.org.starpivot.mall.ums.entity.UmsIntegrationChangeHistory;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.mapper.UmsIntegrationChangeHistoryMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PortalMemberIntegrationServiceImpl implements PortalMemberIntegrationService {

    private static final int SOURCE_ORDER_DEDUCT = 1;
    private static final int SOURCE_ORDER_RESTORE = 2;

    private final UmsMemberMapper umsMemberMapper;
    private final UmsIntegrationChangeHistoryMapper integrationChangeHistoryMapper;
    private final OmsOrderMapper omsOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductForOrder(OmsOrder order) {
        if (order == null || order.getMemberId() == null) {
            return;
        }
        int points = order.getUseIntegration() == null ? 0 : order.getUseIntegration();
        if (points <= 0) {
            return;
        }
        UmsMember member = umsMemberMapper.selectById(order.getMemberId());
        if (member == null) {
            throw new BizException("会员不存在");
        }
        int current = member.getIntegration() == null ? 0 : member.getIntegration();
        if (current < points) {
            throw new BizException("积分不足");
        }
        member.setIntegration(current - points);
        umsMemberMapper.updateById(member);
        writeHistory(order.getMemberId(), -points, "下单抵扣积分，订单号：" + order.getOrderSn(), SOURCE_ORDER_DEDUCT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreForOrder(OmsOrder order) {
        if (order == null || order.getMemberId() == null) {
            return;
        }
        int points = order.getUseIntegration() == null ? 0 : order.getUseIntegration();
        if (points <= 0) {
            return;
        }
        UmsMember member = umsMemberMapper.selectById(order.getMemberId());
        if (member == null) {
            return;
        }
        int current = member.getIntegration() == null ? 0 : member.getIntegration();
        member.setIntegration(current + points);
        umsMemberMapper.updateById(member);
        writeHistory(order.getMemberId(), points, "订单取消回滚积分，订单号：" + order.getOrderSn(), SOURCE_ORDER_RESTORE);
        if (order.getId() != null) {
            OmsOrder patch = new OmsOrder();
            patch.setId(order.getId());
            patch.setUseIntegration(0);
            omsOrderMapper.updateById(patch);
            order.setUseIntegration(0);
        }
    }

    private void writeHistory(Long memberId, int changeCount, String note, int sourceType) {
        UmsIntegrationChangeHistory history = new UmsIntegrationChangeHistory();
        history.setMemberId(memberId);
        history.setChangeCount(changeCount);
        history.setNote(note);
        history.setSourceType(sourceType);
        history.setCreateTime(LocalDateTime.now());
        integrationChangeHistoryMapper.insert(history);
    }
}
