package cn.org.starpivot.monitor.domain.entity;

import lombok.Data;

@Data
public class MonitorUser {

    private Long userId;
    private String userName;
    private String nickName;
    private Long deptId;
}
