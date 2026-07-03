package cn.org.starpivot.mall.member.internal;

import cn.org.starpivot.api.member.dto.MemberOrderIntegrationRequest;
import cn.org.starpivot.common.exception.BizException;
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
public class MemberIntegrationInternalService {

    private static final int SOURCE_ORDER_DEDUCT = 1;
    private static final int SOURCE_ORDER_RESTORE = 2;

    private final UmsMemberMapper umsMemberMapper;
    private final UmsIntegrationChangeHistoryMapper integrationChangeHistoryMapper;

    @Transactional(rollbackFor = Exception.class)
    public void deductForOrder(MemberOrderIntegrationRequest request) {
        if (request == null || request.getMemberId() == null) {
            return;
        }
        int points = request.getUseIntegration() == null ? 0 : request.getUseIntegration();
        if (points <= 0) {
            return;
        }
        UmsMember member = umsMemberMapper.selectById(request.getMemberId());
        if (member == null) {
            throw new BizException("会员不存在");
        }
        int current = member.getIntegration() == null ? 0 : member.getIntegration();
        if (current < points) {
            throw new BizException("积分不足");
        }
        member.setIntegration(current - points);
        umsMemberMapper.updateById(member);
        writeHistory(
                request.getMemberId(),
                -points,
                "下单抵扣积分，订单号：" + request.getOrderSn(),
                SOURCE_ORDER_DEDUCT);
    }

    @Transactional(rollbackFor = Exception.class)
    public void restoreForOrder(MemberOrderIntegrationRequest request) {
        if (request == null || request.getMemberId() == null) {
            return;
        }
        int points = request.getUseIntegration() == null ? 0 : request.getUseIntegration();
        if (points <= 0) {
            return;
        }
        UmsMember member = umsMemberMapper.selectById(request.getMemberId());
        if (member == null) {
            return;
        }
        int current = member.getIntegration() == null ? 0 : member.getIntegration();
        member.setIntegration(current + points);
        umsMemberMapper.updateById(member);
        writeHistory(
                request.getMemberId(),
                points,
                "订单取消回滚积分，订单号：" + request.getOrderSn(),
                SOURCE_ORDER_RESTORE);
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
