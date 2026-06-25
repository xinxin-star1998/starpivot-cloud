package cn.org.starpivot.mall.wms.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 地址视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class AddressVO {

    /**
     * 主键 ID
     */
    private Long id;

    /** 地区编码 */
    /**
     * code
     */
    private String code;

    /** 父级地区编码，省级一般为 0 */
    /**
     * parent Code
     */
    private String parentCode;

    /** 地区名称 */
    /**
     * 名称
     */
    private String name;

    /** 层级：0-省 1-市 2-区县 3-乡镇 */
    /**
     * level
     */
    private Long level;

    /**
     * 创建时间
     */
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /** 树表懒加载：是否还有子级（0～2 为 true，乡镇无下级） */
    /**
     * has Children
     */
    private Boolean hasChildren;

    /** 子级（仅全量树接口使用，懒加载不填充） */
    private List<AddressVO> children = new ArrayList<>();
}
