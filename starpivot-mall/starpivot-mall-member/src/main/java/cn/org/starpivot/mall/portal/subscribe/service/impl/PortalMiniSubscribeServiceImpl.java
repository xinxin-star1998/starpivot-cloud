package cn.org.starpivot.mall.portal.subscribe.service.impl;

import cn.org.starpivot.api.member.dto.MemberSubscribeNotifyRequest;
import cn.org.starpivot.mall.portal.auth.PortalAuthConstants;
import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberAuthService;
import cn.org.starpivot.mall.portal.auth.wechat.WechatMiniProgramClient;
import cn.org.starpivot.mall.portal.subscribe.domain.vo.PortalSubscribeTemplatesVo;
import cn.org.starpivot.mall.portal.subscribe.service.PortalMiniSubscribeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalMiniSubscribeServiceImpl implements PortalMiniSubscribeService {

    private final PortalAuthProperties authProperties;
    private final WechatMiniProgramClient wechatMiniProgramClient;
    private final PortalMemberAuthService portalMemberAuthService;

    @Override
    public PortalSubscribeTemplatesVo templates() {
        PortalAuthProperties.Subscribe subscribe = authProperties.getMiniProgram().getSubscribe();
        String payId = blankToNull(subscribe.getPaySuccess().getTemplateId());
        String deliverId = blankToNull(subscribe.getDeliver().getTemplateId());
        String reviewId = blankToNull(subscribe.getPendingReview().getTemplateId());
        List<String> ids = new ArrayList<>();
        if (payId != null) ids.add(payId);
        if (deliverId != null) ids.add(deliverId);
        if (reviewId != null) ids.add(reviewId);
        boolean enabled = subscribe.isEnabled() && !ids.isEmpty();
        return PortalSubscribeTemplatesVo.builder()
                .enabled(enabled)
                .templateIds(ids)
                .paySuccessTemplateId(payId)
                .deliverTemplateId(deliverId)
                .pendingReviewTemplateId(reviewId)
                .build();
    }

    @Override
    public void notifyQuietly(MemberSubscribeNotifyRequest request) {
        if (request == null || request.getMemberId() == null || !StringUtils.hasText(request.getScene())) {
            return;
        }
        PortalAuthProperties.Subscribe subscribe = authProperties.getMiniProgram().getSubscribe();
        if (!subscribe.isEnabled() || !wechatMiniProgramClient.isConfigured()) {
            return;
        }
        try {
            PortalAuthProperties.SubscribeTemplate template = resolveTemplate(subscribe, request.getScene());
            if (template == null || !StringUtils.hasText(template.getTemplateId())) {
                return;
            }
            String openId;
            try {
                openId = portalMemberAuthService.resolveWechatOpenId(request.getMemberId());
            } catch (Exception ex) {
                log.debug("Skip subscribe: cannot resolve openId for member {}: {}",
                        request.getMemberId(), ex.getMessage());
                return;
            }
            if (!StringUtils.hasText(openId)) {
                return;
            }
            Map<String, String> data = buildData(template, request);
            String page = resolvePage(template.getPage(), request);
            wechatMiniProgramClient.sendSubscribeMessage(openId, template.getTemplateId(), page, data);
        } catch (Exception ex) {
            log.warn("Subscribe notify failed scene={}, memberId={}, orderSn={}: {}",
                    request.getScene(), request.getMemberId(), request.getOrderSn(), ex.getMessage());
        }
    }

    private PortalAuthProperties.SubscribeTemplate resolveTemplate(
            PortalAuthProperties.Subscribe subscribe, String scene) {
        return switch (scene) {
            case PortalAuthConstants.SUBSCRIBE_SCENE_PAY_SUCCESS -> subscribe.getPaySuccess();
            case PortalAuthConstants.SUBSCRIBE_SCENE_DELIVER -> subscribe.getDeliver();
            case PortalAuthConstants.SUBSCRIBE_SCENE_PENDING_REVIEW -> subscribe.getPendingReview();
            default -> null;
        };
    }

    private Map<String, String> buildData(
            PortalAuthProperties.SubscribeTemplate template, MemberSubscribeNotifyRequest request) {
        if (request.getData() != null && !request.getData().isEmpty()) {
            return request.getData();
        }
        List<String> values = sceneValues(request);
        List<String> keys = template.getDataKeys();
        Map<String, String> data = new LinkedHashMap<>();
        if (keys == null || keys.isEmpty()) {
            // 默认字段名（需与微信后台模板一致，可在配置覆盖）
            keys = defaultKeys(request.getScene());
        }
        for (int i = 0; i < keys.size() && i < values.size(); i++) {
            if (StringUtils.hasText(keys.get(i))) {
                data.put(keys.get(i), values.get(i));
            }
        }
        return data;
    }

    private List<String> sceneValues(MemberSubscribeNotifyRequest request) {
        String orderSn = StringUtils.hasText(request.getOrderSn()) ? request.getOrderSn() : "-";
        return switch (request.getScene()) {
            case PortalAuthConstants.SUBSCRIBE_SCENE_PAY_SUCCESS -> List.of(
                    orderSn,
                    StringUtils.hasText(request.getAmountText()) ? request.getAmountText() : "0.00",
                    "支付成功");
            case PortalAuthConstants.SUBSCRIBE_SCENE_DELIVER -> List.of(
                    orderSn,
                    StringUtils.hasText(request.getDeliveryCompany()) ? request.getDeliveryCompany() : "快递配送",
                    StringUtils.hasText(request.getDeliverySn()) ? request.getDeliverySn() : "-");
            case PortalAuthConstants.SUBSCRIBE_SCENE_PENDING_REVIEW -> List.of(
                    orderSn,
                    "待评价",
                    "欢迎分享使用感受");
            default -> List.of(orderSn);
        };
    }

    private List<String> defaultKeys(String scene) {
        return switch (scene) {
            case PortalAuthConstants.SUBSCRIBE_SCENE_PAY_SUCCESS ->
                    List.of("character_string1", "amount2", "thing3");
            case PortalAuthConstants.SUBSCRIBE_SCENE_DELIVER ->
                    List.of("character_string1", "thing2", "character_string3");
            case PortalAuthConstants.SUBSCRIBE_SCENE_PENDING_REVIEW ->
                    List.of("character_string1", "thing2", "thing3");
            default -> List.of("thing1");
        };
    }

    private String resolvePage(String pageTemplate, MemberSubscribeNotifyRequest request) {
        if (StringUtils.hasText(request.getPage())) {
            return request.getPage();
        }
        if (!StringUtils.hasText(pageTemplate)) {
            return null;
        }
        String page = pageTemplate;
        if (request.getOrderId() != null) {
            page = page.replace("{orderId}", String.valueOf(request.getOrderId()));
        }
        if (StringUtils.hasText(request.getOrderSn())) {
            page = page.replace("{orderSn}", request.getOrderSn());
        }
        return page;
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
