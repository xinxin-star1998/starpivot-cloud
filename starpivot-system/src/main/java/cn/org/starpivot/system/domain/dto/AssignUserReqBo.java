package cn.org.starpivot.system.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AssignUserReqBo extends PageReqBo {
    private String userName;
    private String phonenumber;
    @NotBlank(message = "角色ID不能为空")
    private String roleId;
}
