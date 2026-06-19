package cn.org.starpivot.auth.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaptchaResponse {

    private String captchaToken;
    private String captchaImage;
}
