package cn.org.starpivot.mall.ums.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ums_member")
public class UmsMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long levelId;

    private String username;

    private String password;

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
