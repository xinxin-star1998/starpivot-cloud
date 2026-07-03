package cn.org.starpivot.mall.portal.service;

/**
 * 秒杀请求限流（Redis 秒级计数）。
 */
public interface PortalSeckillRateLimitService {

    void check(Long memberId);
}
