package cn.org.starpivot.mall.common;

import cn.org.starpivot.api.mall.promotion.PromotionInternalClient;
import cn.org.starpivot.api.mall.promotion.dto.HomeSubjectDto;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionFeignSupport {

    private static final String ACTION = "营销服务";

    private final PromotionInternalClient promotionInternalClient;

    public int countUnusedCoupons(Long memberId) {
        Integer count = unwrap(promotionInternalClient.countUnusedCoupons(memberId), "优惠券数量加载失败");
        return count == null ? 0 : count;
    }

    public HomeSubjectDto requireSubject(Long subjectId) {
        return unwrap(promotionInternalClient.getSubject(subjectId), "专题不存在");
    }

    private <T> T unwrap(Result<T> result, String notFoundMessage) {
        if (result == null) {
            throw new BizException(ACTION + "不可用");
        }
        if (!result.isSuccess()) {
            if (result.getCode() == ErrorCode.NOT_FOUND) {
                throw new BizException(notFoundMessage);
            }
            throw new BizException(result.getMessage());
        }
        if (result.getData() == null) {
            throw new BizException(notFoundMessage);
        }
        return result.getData();
    }
}
