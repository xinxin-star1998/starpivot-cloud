package cn.org.starpivot.mall.sms.domain.vo;

import java.util.List;
import lombok.Data;

@Data
public class HomeSubjectVo {

    private Long id;
    private String name;
    private String title;
    private String subTitle;
    private Integer status;
    private String url;
    private Integer sort;
    private String img;
    private List<HomeSubjectSpuVo> spuList;
}
