package cn.org.starpivot.api.system.dto;

import lombok.Data;

@Data
public class RegisterUserRequest {

    private String username;

    private String password;
}
