package cn.org.starpivot.mall.wms.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 行政区划 VO（表 address）：0-省 1-市 2-区县 3-乡镇
 */
@Data
public class AddressVO {

    private Long id;

    /** 地区编码 */
    private String code;

    /** 父级地区编码，省级一般为 0 */
    private String parentCode;

    /** 地区名称 */
    private String name;

    /** 层级：0-省 1-市 2-区县 3-乡镇 */
    private Long level;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /** 树表懒加载：是否还有子级（0～2 为 true，乡镇无下级） */
    private Boolean hasChildren;

    /** 子级（仅全量树接口使用，懒加载不填充） */
    private List<AddressVO> children = new ArrayList<>();
}
