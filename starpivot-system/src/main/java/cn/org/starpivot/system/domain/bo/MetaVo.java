package cn.org.starpivot.system.domain.bo;

import lombok.Data;

/**
 * 路由元信息 VO。
 * <p>
 * 描述前端路由的标题、图标、缓存策略等元数据，嵌套于 {@link RouterVo}。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */
@Data
public class MetaVo {

    /**
     * 路由标题（侧边栏及面包屑显示名称）
     */
    private String title;

    /**
     * 路由图标
     */
    private String icon;

    /**
     * 是否不缓存该路由页面
     */
    private boolean noCache;

    /**
     * 内链地址（http(s):// 开头）
     */
    private String link;
}
