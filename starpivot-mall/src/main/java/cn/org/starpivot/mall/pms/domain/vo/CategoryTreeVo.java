package cn.org.starpivot.mall.pms.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类树视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class CategoryTreeVo {

    /**
     * Cat ID
     */
    private Long catId;
    /**
     * 名称
     */
    private String name;
    /**
     * ParentCid
     */
    private Long parentCid;
    /**
     * cat Level
     */
    private Long catLevel;
    /**
     * 状态
     */
    private Long showStatus;
    /**
     * 排序
     */
    private Long sort;
    /**
     * icon
     */
    private String icon;
    /**
     * product Unit
     */
    private String productUnit;
    /**
     * product数量
     */
    private Long productCount;
    private List<CategoryTreeVo> children = new ArrayList<>();
}
