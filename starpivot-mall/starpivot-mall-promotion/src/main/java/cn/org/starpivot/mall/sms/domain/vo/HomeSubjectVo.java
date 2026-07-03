package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 首页专题视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class HomeSubjectVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * title
     */
    private String title;
    /**
     * sub Title
     */
    private String subTitle;
    /**
     * 状态
     */
    private Integer status;
    /**
     * url
     */
    private String url;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 图片
     */
    private String img;
    /**
     * spu List
     */
    private List<HomeSubjectSpuVo> spuList;
}
