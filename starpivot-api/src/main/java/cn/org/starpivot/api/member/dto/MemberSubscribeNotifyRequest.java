package cn.org.starpivot.api.member.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内部：发送小程序订阅消息。
 */
@Data
public class MemberSubscribeNotifyRequest implements Serializable {

    /** pay_success / deliver / pending_review */
    private String scene;

    private Long memberId;

    private Long orderId;

    private String orderSn;

    /** 金额展示文案，如 99.00 */
    private String amountText;

    private String deliveryCompany;

    private String deliverySn;

    private String page;

    /** 覆盖默认字段映射时使用：微信字段名 -> 文本值 */
    private Map<String, String> data = new LinkedHashMap<>();
}
