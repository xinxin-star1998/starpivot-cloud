package cn.org.starpivot.mall.common;

import cn.org.starpivot.api.member.MemberInternalClient;
import cn.org.starpivot.api.member.dto.*;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberFeignSupport {

    private static final String ACTION = "会员服务";

    private final MemberInternalClient memberInternalClient;

    public MemberDto requireMember(Long memberId) {
        return unwrap(memberInternalClient.getMember(memberId), "会员不存在");
    }

    public MemberAddressDto requireAddress(Long memberId, Long addressId) {
        return unwrap(memberInternalClient.getAddress(memberId, addressId), "收货地址不存在");
    }

    public MemberLevelDto requireMemberLevel(Long levelId) {
        if (levelId == null) {
            return null;
        }
        return unwrap(memberInternalClient.getMemberLevel(levelId), "会员等级不存在");
    }

    public String requireWechatOpenId(Long memberId) {
        return unwrap(memberInternalClient.resolveWechatOpenId(memberId), "微信 OpenId 解析失败");
    }

    public void deductIntegrationForOrder(MemberOrderIntegrationRequest request) {
        unwrapVoid(memberInternalClient.deductIntegrationForOrder(request));
    }

    public void restoreIntegrationForOrder(MemberOrderIntegrationRequest request) {
        unwrapVoid(memberInternalClient.restoreIntegrationForOrder(request));
    }

    public void grantRewardOnPaid(MemberOrderRewardRequest request) {
        unwrapVoid(memberInternalClient.grantRewardOnPaid(request));
    }

    public void clawbackRewardOnReturn(MemberOrderReturnRewardRequest request) {
        unwrapVoid(memberInternalClient.clawbackRewardOnReturn(request));
    }

    private <T> T unwrap(Result<T> result, String notFoundMessage) {
        if (result == null) {
            throw new BizException(ACTION + "不可用");
        }
        if (!result.isSuccess()) {
            if (result.getCode() == ErrorCode.NOT_FOUND) {
                throw new BizException(ErrorCode.UNAUTHORIZED, notFoundMessage);
            }
            throw new BizException(result.getMessage());
        }
        if (result.getData() == null) {
            throw new BizException(notFoundMessage);
        }
        return result.getData();
    }

    private void unwrapVoid(Result<Void> result) {
        if (result == null || !result.isSuccess()) {
            throw new BizException(result != null ? result.getMessage() : ACTION + "不可用");
        }
    }
}
