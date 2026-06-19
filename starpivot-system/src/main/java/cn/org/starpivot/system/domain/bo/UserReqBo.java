package cn.org.starpivot.system.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserReqBo extends PageReqBo {
    private String userName;
    private String nickName;
    private String sex;
    private String status;
    private String phonenumber;
    private String email;
    private String roleId;
    private Long deptId;
}
