package cn.org.starpivot.system.domain.dto;

import lombok.Data;

@Data
public class SysConfigQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String configName;

    private String configKey;

    private String configValue;

    private String configType;
}
