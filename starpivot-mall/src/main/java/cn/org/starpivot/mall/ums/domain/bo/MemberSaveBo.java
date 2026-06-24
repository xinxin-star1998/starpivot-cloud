package cn.org.starpivot.mall.ums.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberSaveBo {

    @NotNull(message = "会员ID不能为空")
    private Long id;

    private String nickname;
    private Integer status;
    private Integer integration;
    private Integer growth;
}
