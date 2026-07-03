package cn.org.starpivot.mall.portal.service.support;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.sms.entity.SmsSeckillPromotion;
import cn.org.starpivot.mall.sms.entity.SmsSeckillSession;
import cn.org.starpivot.mall.sms.entity.SmsSeckillSkuRelation;
import cn.org.starpivot.mall.sms.mapper.SmsSeckillPromotionMapper;
import cn.org.starpivot.mall.sms.mapper.SmsSeckillSessionMapper;
import cn.org.starpivot.mall.sms.mapper.SmsSeckillSkuRelationMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 秒杀场次与 SKU 关联校验。
 */
@Component
@RequiredArgsConstructor
public class PortalSeckillSessionSupport {

    private final SmsSeckillPromotionMapper smsSeckillPromotionMapper;
    private final SmsSeckillSessionMapper smsSeckillSessionMapper;
    private final SmsSeckillSkuRelationMapper smsSeckillSkuRelationMapper;

    public record SeckillContext(
            SmsSeckillPromotion promotion,
            SmsSeckillSession session,
            SmsSeckillSkuRelation relation,
            String sessionState) {
    }

    public SmsSeckillPromotion findActivePromotion() {
        LocalDateTime now = LocalDateTime.now();
        return smsSeckillPromotionMapper.selectOne(
                Wrappers.<SmsSeckillPromotion>lambdaQuery()
                        .eq(SmsSeckillPromotion::getStatus, 1)
                        .and(w -> w.isNull(SmsSeckillPromotion::getStartTime).or().le(SmsSeckillPromotion::getStartTime, now))
                        .and(w -> w.isNull(SmsSeckillPromotion::getEndTime).or().ge(SmsSeckillPromotion::getEndTime, now))
                        .orderByDesc(SmsSeckillPromotion::getId)
                        .last("LIMIT 1"));
    }

    public SeckillContext resolve(Long sessionId, Long skuId) {
        SmsSeckillPromotion promotion = findActivePromotion();
        if (promotion == null || promotion.getId() == null) {
            throw new BizException("当前无有效秒杀活动");
        }
        SmsSeckillSession session = smsSeckillSessionMapper.selectById(sessionId);
        if (session == null || !Integer.valueOf(1).equals(session.getStatus())) {
            throw new BizException("秒杀场次不存在或已下线");
        }
        SmsSeckillSkuRelation relation = smsSeckillSkuRelationMapper.selectOne(
                Wrappers.<SmsSeckillSkuRelation>lambdaQuery()
                        .eq(SmsSeckillSkuRelation::getPromotionId, promotion.getId())
                        .eq(SmsSeckillSkuRelation::getPromotionSessionId, sessionId)
                        .eq(SmsSeckillSkuRelation::getSkuId, skuId)
                        .last("LIMIT 1"));
        if (relation == null) {
            throw new BizException("该商品未参与本场秒杀");
        }
        return new SeckillContext(promotion, session, relation, resolveState(session));
    }

    public String resolveState(SmsSeckillSession session) {
        LocalTime now = LocalTime.now();
        LocalTime start = session.getStartTime() != null ? session.getStartTime().toLocalTime() : LocalTime.MIN;
        LocalTime end = session.getEndTime() != null ? session.getEndTime().toLocalTime() : LocalTime.MAX;
        if (!now.isBefore(start) && now.isBefore(end)) {
            return "ongoing";
        }
        if (now.isBefore(start)) {
            return "upcoming";
        }
        return "ended";
    }
}
