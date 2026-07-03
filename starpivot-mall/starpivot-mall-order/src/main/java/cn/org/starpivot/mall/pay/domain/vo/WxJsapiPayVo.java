package cn.org.starpivot.mall.pay.domain.vo;

import lombok.Data;

/**
 * 微信小程序 JSAPI 支付参数（供 wx.requestPayment 使用）。
 */
@Data
public class WxJsapiPayVo {

    private String orderSn;

    private boolean mock;

    private String timeStamp;

    private String nonceStr;

    /** 对应 wx.requestPayment 的 package 字段 */
    private String packageValue;

    private String signType;

    private String paySign;
}
