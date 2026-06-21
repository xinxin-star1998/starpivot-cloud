package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysLogininfor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {

    List<Map<String, Object>> countByMonthRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Map<String, Object>> countByUserNames(@Param("userNames") List<String> userNames, @Param("start") LocalDateTime start);
}
