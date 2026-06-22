package cn.org.starpivot.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体公共基类，统一审计字段与备注。
 * <p>
 * 业务实体可继承此类以复用创建/更新人、时间及备注字段；代码生成模块亦将其映射为通用列。
 * </p>
 */
@Data
public class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 最后更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /** 创建者用户名 */
    private String createBy;

    /** 最后更新者用户名 */
    private String updateBy;

    /** 备注 */
    private String remark;
}
