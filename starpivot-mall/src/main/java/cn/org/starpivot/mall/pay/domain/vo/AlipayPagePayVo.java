package cn.org.starpivot.mall.pay.domain.vo;

import lombok.Data;

/**
 * 支付宝电脑网站支付响应：前端注入并自动提交 form。
 */
@Data
public class AlipayPagePayVo {

    private String orderSn;

    /** 支付宝返回的 HTML form，可直接 document.write 后 submit */
    private String payForm;
}
