package cn.org.starpivot.system.domain.bo;

import lombok.Data;

@Data
public class SysConfigVO {

    private Long configId;

    private String configName;

    private String configKey;

    private String configValue;

    private String configType;

    private String remark;
}
