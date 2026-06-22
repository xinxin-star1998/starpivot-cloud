package cn.org.starpivot.monitor.domain.entity;

import lombok.Data;

/**
 * 监控模块使用的部门精简实体，映射 {@code sys_dept} 表关键字段。
 * <p>
 * {@link Data}：生成 getter/setter 及 equals/hashCode。
 */
@Data
public class MonitorDept {

    /** 部门 ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;
}
