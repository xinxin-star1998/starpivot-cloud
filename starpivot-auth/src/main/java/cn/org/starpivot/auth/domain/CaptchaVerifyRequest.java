package cn.org.starpivot.auth.domain;

import lombok.Data;

@Data
public class CaptchaVerifyRequest {

    private String captchaToken;
    private String code;
    private String scene;
}
