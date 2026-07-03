package cn.org.starpivot.mall.sms.domain.vo;

import lombok.Data;

/**
 * 首页专题 SPU视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class HomeSubjectSpuVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * Subject ID
     */
    private Long subjectId;
    /**
     * SPU ID
     */
    private Long spuId;
    /**
     * 名称
     */
    private String name;
    /**
     * spu名称
     */
    private String spuName;
    /**
     * 排序
     */
    private Integer sort;
}
