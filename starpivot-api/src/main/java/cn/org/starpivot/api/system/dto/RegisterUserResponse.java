package cn.org.starpivot.api.system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserResponse {

    private Long userId;

    private String username;

    private String nickName;
}
