package cn.org.starpivot.system.domain.dto;

import lombok.Data;

@Data
public class SysConfigDTO {

    private Long configId;

    private String configName;

    private String configKey;

    private String configValue;

    private String configType;

    private String remark;
}
