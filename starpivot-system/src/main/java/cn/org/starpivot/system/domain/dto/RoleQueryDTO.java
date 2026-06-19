package cn.org.starpivot.system.domain.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RoleQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String roleName;

    private String roleKey;

    private String status;

    private Date startTime;

    private Date endTime;
}
