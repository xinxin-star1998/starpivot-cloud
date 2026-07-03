package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 微信小程序 jscode2session（真实调用）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMiniProgramClientImpl implements WechatMiniProgramClient {

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    private final PortalAuthProperties authProperties;
    private final ObjectMapper objectMapper;
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
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            assertWechatOk(json);
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

    private void assertWechatOk(JsonNode json) {
        if (json == null || !json.isObject()) {
            throw new BizException("微信登录响应无效");
        }
        if (json.has("errcode") && json.get("errcode").asInt() != 0) {
            String msg = json.has("errmsg") ? json.get("errmsg").asText() : "微信登录失败";
            throw new BizException(msg);
        }
        if (!json.has("openid") || !StringUtils.hasText(json.get("openid").asText())) {
            throw new BizException("微信登录未返回 openid");
        }
    }

    private static String text(JsonNode json, String field) {
        return json.has(field) && !json.get(field).isNull() ? json.get(field).asText() : null;
    }
}
