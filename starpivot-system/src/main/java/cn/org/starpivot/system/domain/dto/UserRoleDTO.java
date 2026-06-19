package cn.org.starpivot.system.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserRoleDTO {

    private Long roleId;

    private List<Long> userIds;
}
