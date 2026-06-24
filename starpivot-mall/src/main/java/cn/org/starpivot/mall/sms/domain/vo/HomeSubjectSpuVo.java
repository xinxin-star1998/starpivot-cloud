package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

@Data
public class HomeSubjectSpuVo {

    private Long id;
    private Long subjectId;
    private Long spuId;
    private String name;
    private String spuName;
    private Integer sort;
}
