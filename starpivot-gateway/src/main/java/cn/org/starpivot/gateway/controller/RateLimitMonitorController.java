package cn.org.starpivot.gateway.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.gateway.config.GatewayRateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.org.starpivot.common.cache.CacheConstants.gatewayRateLimitBlockedKey;
import static cn.org.starpivot.common.cache.CacheConstants.gatewayRateLimitTotalKey;

/**
 * 网关限流统计监控端点。
 * <p>
 * 暴露各限流规则的请求总量与被拦截次数，供运维大盘消费。
 * </p>
 */
@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class RateLimitMonitorController {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final GatewayRateLimitProperties rateLimitProperties;

    /**
     * 查询各限流规则的统计数据。
     *
     * @return 每条规则对应 {@code ruleId / total / blocked} 的列表
     */
    @GetMapping("/rate-limit")
    public Mono<Result<List<Map<String, Object>>>> rateLimitStats() {
        List<GatewayRateLimitProperties.Rule> rules = rateLimitProperties.getRules();
        if (rules == null || rules.isEmpty()) {
            return Mono.just(Result.success(List.of()));
        }

        return Flux.fromIterable(rules)
                .flatMap(rule -> {
                    String totalKey = gatewayRateLimitTotalKey(rule.getId());
                    String blockedKey = gatewayRateLimitBlockedKey(rule.getId());

                    Mono<Long> totalMono = redisTemplate.opsForValue().get(totalKey)
                            .map(Long::parseLong)
                            .defaultIfEmpty(0L);
                    Mono<Long> blockedMono = redisTemplate.opsForValue().get(blockedKey)
                            .map(Long::parseLong)
                            .defaultIfEmpty(0L);

                    return Mono.zip(totalMono, blockedMono)
                            .map(tuple -> {
                                Map<String, Object> stat = new LinkedHashMap<>();
                                stat.put("ruleId", rule.getId());
                                stat.put("pattern", rule.getPattern());
                                stat.put("limit", rule.getLimit());
                                stat.put("windowSeconds", rule.getWindowSeconds());
                                stat.put("total", tuple.getT1());
                                stat.put("blocked", tuple.getT2());
                                return stat;
                            });
                })
                .collectList()
                .map(Result::success);
    }
}
