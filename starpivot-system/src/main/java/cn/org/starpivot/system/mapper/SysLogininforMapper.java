package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysLogininfor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 登录日志 Mapper 接口。
 * <p>提供登录日志统计聚合的自定义 SQL。</p>
 */
@Mapper
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {

    /** 按月份统计登录次数。 */
    List<Map<String, Object>> countByMonthRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 按用户名列表统计登录次数。 */
    List<Map<String, Object>> countByUserNames(@Param("userNames") List<String> userNames, @Param("start") LocalDateTime start);
}
