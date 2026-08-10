package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.auth.PortalAuthConstants;
import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * 微信小程序真实 API（code2session / 手机号 / 订阅消息）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMiniProgramClientImpl implements WechatMiniProgramClient {

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
    private static final String TOKEN_URL =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String PHONE_URL =
            "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s";
    private static final String SUBSCRIBE_SEND_URL =
            "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s";

    private final PortalAuthProperties authProperties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public boolean isConfigured() {
        PortalAuthProperties.MiniProgram mini = authProperties.getMiniProgram();
        return mini.isEnabled()
                && StringUtils.hasText(mini.getAppId())
                && StringUtils.hasText(mini.getAppSecret());
    }

    @Override
    public WechatUserProfile code2Session(String code) {
        PortalAuthProperties.MiniProgram mini = authProperties.getMiniProgram();
        try {
            String url = String.format(CODE2SESSION_URL, mini.getAppId(), mini.getAppSecret(), code);
            JsonNode json = getJson(url);
            assertWechatOk(json, "微信登录");
            return WechatUserProfile.builder()
                    .unionId(text(json, "unionid"))
                    .openId(text(json, "openid"))
                    .appId(mini.getAppId())
                    .build();
        } catch (BizException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Mini program code2session failed", e);
            throw new BizException("微信登录失败，请重试");
        }
    }

    @Override
    public String getPhoneNumber(String phoneCode) {
        if (!StringUtils.hasText(phoneCode)) {
            throw new BizException("手机号授权码不能为空");
        }
        try {
            String token = requireAccessToken();
            ObjectNode body = objectMapper.createObjectNode();
            body.put("code", phoneCode);
            JsonNode json = postJson(String.format(PHONE_URL, token), body.toString());
            assertWechatOk(json, "获取手机号");
            JsonNode phoneInfo = json.get("phone_info");
            if (phoneInfo == null || phoneInfo.isNull()) {
                throw new BizException("微信未返回手机号信息");
            }
            String pure = text(phoneInfo, "purePhoneNumber");
            if (!StringUtils.hasText(pure)) {
                pure = text(phoneInfo, "phoneNumber");
            }
            if (!StringUtils.hasText(pure)) {
                throw new BizException("微信未返回有效手机号");
            }
            // 去掉国际区号前缀
            if (pure.startsWith("+86")) {
                pure = pure.substring(3);
            } else if (pure.startsWith("86") && pure.length() > 11) {
                pure = pure.substring(2);
            }
            return pure.trim();
        } catch (BizException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Mini program getPhoneNumber failed", e);
            throw new BizException("获取微信手机号失败，请重试");
        }
    }

    @Override
    public void sendSubscribeMessage(String openId, String templateId, String page, Map<String, String> data) {
        if (!StringUtils.hasText(openId) || !StringUtils.hasText(templateId)) {
            throw new BizException("订阅消息参数不完整");
        }
        try {
            String token = requireAccessToken();
            ObjectNode body = objectMapper.createObjectNode();
            body.put("touser", openId);
            body.put("template_id", templateId);
            if (StringUtils.hasText(page)) {
                body.put("page", page);
            }
            body.put("miniprogram_state", "formal");
            body.put("lang", "zh_CN");
            ObjectNode dataNode = body.putObject("data");
            Map<String, String> safeData = data != null ? data : Map.of();
            for (Map.Entry<String, String> entry : safeData.entrySet()) {
                if (!StringUtils.hasText(entry.getKey())) {
                    continue;
                }
                ObjectNode field = dataNode.putObject(entry.getKey());
                String value = entry.getValue() == null ? "" : entry.getValue();
                // 微信字段值有长度限制，截断以避免发送失败
                if (value.length() > 20) {
                    value = value.substring(0, 20);
                }
                field.put("value", value);
            }
            JsonNode json = postJson(String.format(SUBSCRIBE_SEND_URL, token), body.toString());
            assertWechatOk(json, "订阅消息");
        } catch (BizException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Mini program subscribe send failed", e);
            throw new BizException("订阅消息发送失败");
        }
    }

    private String requireAccessToken() throws Exception {
        String cacheKey = PortalAuthConstants.miniAccessTokenKey(
                authProperties.getMiniProgram().getAppId());
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            return cached;
        }
        PortalAuthProperties.MiniProgram mini = authProperties.getMiniProgram();
        String url = String.format(TOKEN_URL, mini.getAppId(), mini.getAppSecret());
        JsonNode json = getJson(url);
        assertWechatOk(json, "获取 access_token");
        String token = text(json, "access_token");
        if (!StringUtils.hasText(token)) {
            throw new BizException("微信未返回 access_token");
        }
        int expires = json.has("expires_in") ? json.get("expires_in").asInt(7200) : 7200;
        // 提前 5 分钟过期
        long ttl = Math.max(60, expires - 300);
        stringRedisTemplate.opsForValue().set(cacheKey, token, Duration.ofSeconds(ttl));
        return token;
    }

    private JsonNode getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    private JsonNode postJson(String url, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(15))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    private void assertWechatOk(JsonNode json, String action) {
        if (json == null || !json.isObject()) {
            throw new BizException(action + "响应无效");
        }
        if (json.has("errcode") && json.get("errcode").asInt() != 0) {
            String msg = json.has("errmsg") ? json.get("errmsg").asText() : action + "失败";
            throw new BizException(msg);
        }
        if ("微信登录".equals(action)
                && (!json.has("openid") || !StringUtils.hasText(json.get("openid").asText()))) {
            throw new BizException("微信登录未返回 openid");
        }
    }

    private static String text(JsonNode json, String field) {
        return json.has(field) && !json.get(field).isNull() ? json.get(field).asText() : null;
    }
}
