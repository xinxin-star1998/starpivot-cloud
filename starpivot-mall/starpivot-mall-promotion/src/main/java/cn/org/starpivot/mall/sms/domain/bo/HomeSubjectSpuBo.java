package cn.org.starpivot.mall.sms.domain.bo;

import lombok.Data;

/**
 * 首页专题 SPU请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class HomeSubjectSpuBo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * SPU ID
     */
    private Long spuId;
    /**
     * 名称
     */
    private String name;
    /**
     * 排序
     */
    private Integer sort;
}
