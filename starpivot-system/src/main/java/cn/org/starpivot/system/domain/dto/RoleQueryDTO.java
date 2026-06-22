package cn.org.starpivot.system.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 角色分页查询 DTO。
 * <p>
 * 封装角色列表接口的筛选条件与分页参数。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */
@Data
public class RoleQueryDTO {

    /**
     * 当前页码，默认第 1 页
     */
    private Integer pageNum = 1;

    /**
     * 每页条数，默认 10 条
     */
    private Integer pageSize = 10;

    /**
     * 角色名称（模糊匹配）
     */
    private String roleName;

    /**
     * 角色权限字符串（模糊匹配）
     */
    private String roleKey;

    /**
     * 角色状态（0正常 1停用）
     */
    private String status;

    /**
     * 创建时间范围起始
     */
    private Date startTime;

    /**
     * 创建时间范围结束
     */
    private Date endTime;
}
