package cn.org.starpivot.system.domain.bo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class RouterVo {

    private String name;

    private Long menuId;

    private String perms;

    private String menuType;

    private Integer isFrame;

    private String path;

    private boolean hidden;

    private String redirect;

    private String component;

    private String query;

    private Boolean alwaysShow;

    private MetaVo meta;

    private List<RouterVo> children;
}
