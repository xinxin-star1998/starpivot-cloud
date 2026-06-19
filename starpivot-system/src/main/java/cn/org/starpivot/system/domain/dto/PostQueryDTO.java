package cn.org.starpivot.system.domain.dto;

import lombok.Data;

@Data
public class PostQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String postCode;

    private String postName;

    private String status;
}
