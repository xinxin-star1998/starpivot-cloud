package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalOrderTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortalOrderTokenServiceImpl implements PortalOrderTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String issueSubmitToken(Long memberId) {
        if (memberId == null) {
            throw new BizException("会员未登录");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(
                submitTokenKey(memberId, token),
                "1",
                Duration.ofMinutes(PortalConstants.ORDER_SUBMIT_TOKEN_TTL_MINUTES));
        return token;
    }

    @Override
    public void consumeSubmitToken(Long memberId, String token) {
        if (memberId == null) {
            throw new BizException("会员未登录");
        }
        if (!StringUtils.hasText(token)) {
            throw new BizException("订单提交令牌不能为空");
        }
        String key = submitTokenKey(memberId, token.trim());
        Boolean removed = stringRedisTemplate.delete(key);
        if (!Boolean.TRUE.equals(removed)) {
            throw new BizException("订单提交令牌无效或已过期，请刷新页面后重试");
        }
    }

    private String submitTokenKey(Long memberId, String token) {
        return PortalConstants.ORDER_SUBMIT_TOKEN_PREFIX + memberId + ":" + token;
    }
}
