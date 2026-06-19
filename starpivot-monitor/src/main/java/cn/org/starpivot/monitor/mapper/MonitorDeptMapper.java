package cn.org.starpivot.monitor.mapper;

import cn.org.starpivot.monitor.domain.entity.MonitorDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface MonitorDeptMapper {

    @Select("""
            <script>
            SELECT dept_id AS deptId, dept_name AS deptName
            FROM sys_dept
            WHERE dept_id IN
            <foreach collection='deptIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            AND del_flag = '0'
            </script>
            """)
    List<MonitorDept> selectByDeptIds(@Param("deptIds") Collection<Long> deptIds);

    @Select("SELECT dept_id AS deptId, dept_name AS deptName FROM sys_dept WHERE dept_id = #{deptId} AND del_flag = '0'")
    MonitorDept selectByDeptId(@Param("deptId") Long deptId);
}
