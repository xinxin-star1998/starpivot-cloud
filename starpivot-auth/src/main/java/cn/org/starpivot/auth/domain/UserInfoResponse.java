package cn.org.starpivot.auth.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserInfoResponse {

    private UserVo user;
    private List<RoleVo> roles;
    private List<Object> permissions;

    @Data
    @Builder
    public static class UserVo {
        private Long userId;
        private String username;
        private String nickName;
        private String avatar;
        private String email;
        private String phoneNumber;
        private Integer sex;
        private String status;
        private String createTime;
    }

    @Data
    @Builder
    public static class RoleVo {
        private Long roleId;
        private String roleName;
        private String roleKey;
        private Integer roleSort;
        private String status;
        private String createTime;
    }
}
