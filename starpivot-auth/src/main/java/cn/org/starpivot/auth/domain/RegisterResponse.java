package cn.org.starpivot.auth.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    private Long userId;

    private String username;

    private String nickName;
}
