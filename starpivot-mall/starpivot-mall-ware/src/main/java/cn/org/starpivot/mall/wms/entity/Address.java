package cn.org.starpivot.mall.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 地址实体。
 * <p>
 * 对应数据库表 {@code address}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("address")
public class Address implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO, value = "id")
    private Long id;

    /**
     * code
     */
    private String code;

    /**
     * parent Code
     */
    private String parentCode;

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

@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */

@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * is Delete
     */
    private Boolean isDelete;
}
