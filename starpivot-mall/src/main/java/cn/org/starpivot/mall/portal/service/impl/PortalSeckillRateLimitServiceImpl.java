package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.config.MallSeckillProperties;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalSeckillRateLimitService;
import cn.org.starpivot.mall.sms.entity.SmsSeckillSkuRelation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PortalSeckillRateLimitServiceImpl implements PortalSeckillRateLimitService {

    private final StringRedisTemplate stringRedisTemplate;
    private final MallSeckillProperties mallSeckillProperties;

    @Override
    public void check(Long memberId) {
        if (memberId == null) {
            throw new BizException("请先登录");
        }
        int max = Math.max(1, mallSeckillProperties.getMaxRequestsPerSecond());
        String key = PortalConstants.seckillRateKey(memberId);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(1));
        }
        if (count != null && count > max) {
            throw new BizException("操作过于频繁，请稍后再试");
        }
    }
}
