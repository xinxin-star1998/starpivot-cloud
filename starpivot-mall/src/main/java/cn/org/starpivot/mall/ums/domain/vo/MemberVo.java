package cn.org.starpivot.mall.ums.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MemberVo {

    private Long id;
    private Long levelId;
    private String username;
    private String nickname;
    private String mobile;
    private String email;
    private String header;
    private Integer gender;
    private LocalDate birth;
    private String city;
    private String job;
    private String sign;
    private Integer sourceType;
    private Integer integration;
    private Integer growth;
    private Integer status;
    private LocalDateTime createTime;
    private String socialUid;
    private String accessToken;
    private String expiresIn;
}
