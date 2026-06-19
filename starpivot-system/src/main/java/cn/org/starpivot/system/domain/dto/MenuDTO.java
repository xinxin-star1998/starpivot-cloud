package cn.org.starpivot.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuDTO {

    private Long menuId;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    private Long parentId;

    private Integer orderNum;

    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    private String path;

    @Size(max = 255, message = "组件路径长度不能超过255个字符")
    private String component;

    private String query;

    private String routeName;

    private Integer isFrame;

    private Integer isCache;

    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    private String visible;

    private String status;

    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String perms;

    private String icon;

    private String remark;
}
