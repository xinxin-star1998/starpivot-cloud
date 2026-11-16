package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.config.MallSeckillProperties;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalSeckillRateLimitService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortalSeckillRateLimitServiceImpl implements PortalSeckillRateLimitService {

    private final RedissonClient redissonClient;
    private final MallSeckillProperties mallSeckillProperties;

    @Override
    public void check(Long memberId) {
        if (memberId == null) {
            throw new BizException("请先登录");
        }
        int max = Math.max(1, mallSeckillProperties.getMaxRequestsPerSecond());
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(PortalConstants.seckillRateKey(memberId));
        // 仅首次初始化速率；修改配置后需删除对应 key 生效
        rateLimiter.trySetRate(RateType.OVERALL, max, 1, RateIntervalUnit.SECONDS);
        if (!rateLimiter.tryAcquire()) {
            throw new BizException("操作过于频繁，请稍后再试");
        }
    }
}
