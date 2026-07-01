package cn.org.starpivot.mall.portal.service;

/**
 * C 端下单防重令牌（Redis 一次性）。
 */
public interface PortalOrderTokenService {

    /** 签发令牌，有效期默认 30 分钟 */
    String issueSubmitToken(Long memberId);

    /** 校验并消费令牌 */
    void consumeSubmitToken(Long memberId, String token);
}
