package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.member.MemberInternalClient;
import cn.org.starpivot.api.member.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

public class MemberInternalClientFallbackFactory implements FallbackFactory<MemberInternalClient> {

    private static final String ACTION = "会员服务";

    @Override
    public MemberInternalClient create(Throwable cause) {
        return new MemberInternalClient() {
            @Override
            public Result<MemberDto> getMember(Long memberId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<MemberAddressDto> getAddress(Long memberId, Long addressId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<MemberLevelDto> getMemberLevel(Long levelId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<String> resolveWechatOpenId(Long memberId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> deductIntegrationForOrder(MemberOrderIntegrationRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> restoreIntegrationForOrder(MemberOrderIntegrationRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> grantRewardOnPaid(MemberOrderRewardRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> clawbackRewardOnReturn(MemberOrderReturnRewardRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
