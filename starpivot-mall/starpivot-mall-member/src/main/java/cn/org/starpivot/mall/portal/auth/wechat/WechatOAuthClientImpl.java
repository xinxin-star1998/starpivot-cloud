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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 微信开放平台 / 公众号 OAuth（真实调用）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatOAuthClientImpl implements WechatOAuthClient {

    private static final String QR_CONNECT =
            "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect";
    private static final String MP_AUTHORIZE =
            "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=%s#wechat_redirect";
    private static final String ACCESS_TOKEN_URL =
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    private static final String USER_INFO_URL =
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN";

    private final PortalAuthProperties authProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public boolean isConfigured() {
        PortalAuthProperties.Wechat wechat = authProperties.getWechat();
        return wechat.isEnabled()
                && StringUtils.hasText(wechat.getAppId())
                && StringUtils.hasText(wechat.getAppSecret())
                && StringUtils.hasText(wechat.getRedirectUri());
    }

    @Override
    public String buildAuthorizeUrl(String state, String callbackUri) {
        PortalAuthProperties.Wechat wechat = authProperties.getWechat();
        String encodedRedirect = URLEncoder.encode(wechat.getRedirectUri(), StandardCharsets.UTF_8);
        if ("mp".equalsIgnoreCase(wechat.getMode())) {
            return String.format(MP_AUTHORIZE, wechat.getAppId(), encodedRedirect, state);
        }
        return String.format(QR_CONNECT, wechat.getAppId(), encodedRedirect, state);
    }

    @Override
    public WechatUserProfile exchangeCode(String code) {
        PortalAuthProperties.Wechat wechat = authProperties.getWechat();
        try {
            String tokenUrl = String.format(ACCESS_TOKEN_URL, wechat.getAppId(), wechat.getAppSecret(), code);
            JsonNode tokenJson = getJson(tokenUrl);
            assertWechatOk(tokenJson);

            String accessToken = text(tokenJson, "access_token");
            String openId = text(tokenJson, "openid");
            String unionId = text(tokenJson, "unionid");

            WechatUserProfile.WechatUserProfileBuilder builder = WechatUserProfile.builder()
                    .openId(openId)
                    .unionId(unionId)
                    .appId(wechat.getAppId());

            if (StringUtils.hasText(accessToken) && StringUtils.hasText(openId)) {
                String userUrl = String.format(USER_INFO_URL, accessToken, openId);
                JsonNode userJson = getJson(userUrl);
                if (userJson != null && !userJson.has("errcode")) {
                    builder.nickname(text(userJson, "nickname"))
                            .avatar(text(userJson, "headimgurl"));
                    if (!StringUtils.hasText(unionId)) {
                        builder.unionId(text(userJson, "unionid"));
                    }
                }
            }
            return builder.build();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("WeChat OAuth exchange failed", e);
            throw new BizException("微信授权失败，请重试");
        }
    }

    private JsonNode getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readTree(response.body());
    }

    private void assertWechatOk(JsonNode json) {
        if (json == null) {
            throw new BizException("微信授权响应无效");
        }
        if (json.has("errcode") && json.get("errcode").asInt() != 0) {
            String msg = json.has("errmsg") ? json.get("errmsg").asText() : "微信授权失败";
            throw new BizException(msg);
        }
    }

    private static String text(JsonNode json, String field) {
        if (json == null || !json.has(field) || json.get(field).isNull()) {
            return null;
        }
        return json.get(field).asText();
    }
}
