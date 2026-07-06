package cn.org.starpivot.ai.mapper;

import cn.org.starpivot.ai.domain.entity.AiUsageLog;
import cn.org.starpivot.ai.domain.vo.AiUsageSummaryVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiUsageLogMapper extends BaseMapper<AiUsageLog> {

    AiUsageSummaryVo selectSummary(@Param("beginTime") String beginTime, @Param("endTime") String endTime);
}
