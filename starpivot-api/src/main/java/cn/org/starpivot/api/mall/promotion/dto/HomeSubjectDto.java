package cn.org.starpivot.api.mall.promotion.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HomeSubjectDto implements Serializable {

    private Long id;

    private String name;

    private String title;

    private Integer status;

    private String url;

    private String img;
}
