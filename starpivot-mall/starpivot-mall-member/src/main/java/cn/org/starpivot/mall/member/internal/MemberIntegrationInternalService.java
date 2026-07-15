package cn.org.starpivot.mall.member.internal;

import cn.org.starpivot.api.member.dto.MemberOrderIntegrationRequest;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.ums.entity.UmsIntegrationChangeHistory;
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
        if (umsMemberMapper.selectById(request.getMemberId()) == null) {
            throw new BizException("会员不存在");
        }
        // 原子扣减积分（带余额校验），避免并发竞态
        int affected = umsMemberMapper.deductIntegrationIfSufficient(request.getMemberId(), points);
        if (affected == 0) {
            throw new BizException("积分不足");
        }
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
        // 原子恢复积分；会员不存在时不写入流水，避免虚假审计记录
        int affected = umsMemberMapper.addIntegration(request.getMemberId(), points);
        if (affected == 0) {
            throw new BizException("会员不存在，无法回滚积分");
        }
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
