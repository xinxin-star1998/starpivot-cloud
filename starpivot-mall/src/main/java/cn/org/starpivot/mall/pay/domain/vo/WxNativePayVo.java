package cn.org.starpivot.mall.pay.domain.vo;

import lombok.Data;

/** 微信 Native 支付下单结果 */
@Data
public class WxNativePayVo {

    private String orderSn;

    /** 用于生成二维码的 code_url */
    private String codeUrl;

    /** 是否为开发 Mock */
    private Boolean mock;
}
