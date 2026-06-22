package cn.org.starpivot.system.domain.bo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 前端动态路由 VO。
 * <p>
 * 将后台菜单树转换为前端 Vue Router 可识别的路由结构。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link JsonInclude} — 序列化时忽略空集合与空字符串字段</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class RouterVo {

    /**
     * 路由名称
     */
    private String name;

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 权限标识
     */
    private String perms;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    private String menuType;

    /**
     * 是否为外链（0是 1否）
     */
    private Integer isFrame;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 是否在侧边栏隐藏
     */
    private boolean hidden;

    /**
     * 重定向地址
     */
    private String redirect;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 路由参数
     */
    private String query;

    /**
     * 当子路由仅有一个时是否始终显示父级菜单
     */
    private Boolean alwaysShow;

    /**
     * 路由元信息
     */
    private MetaVo meta;

    /**
     * 子路由列表
     */
    private List<RouterVo> children;
}
