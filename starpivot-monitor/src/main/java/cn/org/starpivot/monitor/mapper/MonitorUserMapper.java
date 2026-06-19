package cn.org.starpivot.monitor.mapper;

import cn.org.starpivot.monitor.domain.entity.MonitorUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface MonitorUserMapper {

    @Select("""
            <script>
            SELECT user_id AS userId, user_name AS userName, nick_name AS nickName, dept_id AS deptId
            FROM sys_user
            WHERE user_id IN
            <foreach collection='userIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>
            AND del_flag = '0'
            </script>
            """)
    List<MonitorUser> selectByUserIds(@Param("userIds") Collection<Long> userIds);

    @Select("""
            SELECT user_id AS userId, user_name AS userName, nick_name AS nickName, dept_id AS deptId
            FROM sys_user WHERE user_id = #{userId} AND del_flag = '0'
            """)
    MonitorUser selectByUserId(@Param("userId") Long userId);
}
