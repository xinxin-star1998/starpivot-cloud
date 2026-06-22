package cn.org.starpivot.monitor.mapper;

import cn.org.starpivot.monitor.domain.entity.MonitorDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 监控模块部门查询 Mapper，只读访问 {@code sys_dept}。
 * <p>
 * {@link Mapper}：注册为 MyBatis Mapper 接口。
 */
@Mapper
public interface MonitorDeptMapper {

    /**
     * 按部门 ID 集合批量查询未删除部门。
     *
     * @param deptIds 部门 ID 集合
     * @return 部门列表
     */
    List<MonitorDept> selectByDeptIds(@Param("deptIds") Collection<Long> deptIds);

    /**
     * 按部门 ID 查询单个未删除部门。
     *
     * @param deptId 部门 ID
     * @return 部门信息，不存在时返回 {@code null}
     */
    MonitorDept selectByDeptId(@Param("deptId") Long deptId);
}
