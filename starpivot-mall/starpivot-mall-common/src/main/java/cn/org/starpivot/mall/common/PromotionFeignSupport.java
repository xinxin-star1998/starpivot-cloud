package cn.org.starpivot.mall.common;

import cn.org.starpivot.api.mall.order.dto.OrderRewardContextDto;
import cn.org.starpivot.api.mall.promotion.PromotionInternalClient;
import cn.org.starpivot.api.mall.promotion.dto.HomeSubjectDto;
import cn.org.starpivot.api.mall.promotion.dto.SpuBoundsUpsertRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    /**
     * 查询 SPU 积分配置。服务调用失败会抛错；成功但无记录时返回 {@code null}。
     */
    public OrderRewardContextDto.SpuBoundsLine findSpuBounds(Long spuId) {
        if (spuId == null) {
            return null;
        }
        Result<Map<Long, OrderRewardContextDto.SpuBoundsLine>> result =
                promotionInternalClient.batchSpuBounds(List.of(spuId));
        if (result == null) {
            throw new BizException(ACTION + "不可用");
        }
        if (!result.isSuccess()) {
            throw new BizException(result.getMessage());
        }
        Map<Long, OrderRewardContextDto.SpuBoundsLine> data = result.getData();
        if (data == null) {
            return null;
        }
        return data.get(spuId);
    }

    public void syncSpuBounds(Long spuId, BigDecimal buyBounds, BigDecimal growBounds) {
        if (spuId == null) {
            return;
        }
        SpuBoundsUpsertRequest request = new SpuBoundsUpsertRequest();
        request.setSpuId(spuId);
        request.setBuyBounds(buyBounds == null ? BigDecimal.ZERO : buyBounds);
        request.setGrowBounds(growBounds == null ? BigDecimal.ZERO : growBounds);
        Result<Void> result = promotionInternalClient.upsertSpuBounds(request);
        if (result == null || !result.isSuccess()) {
            throw new BizException(result != null ? result.getMessage() : ACTION + "积分配置保存失败");
        }
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
