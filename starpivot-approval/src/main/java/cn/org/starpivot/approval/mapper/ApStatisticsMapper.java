package cn.org.starpivot.approval.mapper;

import cn.org.starpivot.approval.domain.vo.ApStatisticsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ApStatisticsMapper {

    @Select("SELECT AVG(TIMESTAMPDIFF(MINUTE, create_time, finish_time)) / 60.0 " +
            "FROM ap_instance WHERE finish_time IS NOT NULL " +
            "AND status IN ('APPROVED', 'REJECTED', 'WITHDRAWN')")
    Double avgFinishHours();

    @Select("SELECT DATE_FORMAT(finish_time, '%Y-%m-%d') AS day, COUNT(*) AS count " +
            "FROM ap_instance WHERE finish_time >= #{since} AND finish_time IS NOT NULL " +
            "GROUP BY DATE(finish_time) ORDER BY day")
    List<ApStatisticsVo.DailyFinishedItem> dailyFinished(@Param("since") LocalDateTime since);

    @Select("<script>" +
            "SELECT biz_type AS bizType, COUNT(*) AS total, " +
            "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approved, " +
            "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected " +
            "FROM ap_instance " +
            "<if test='bizModule != null and bizModule != \"\"'>WHERE biz_module = #{bizModule}</if> " +
            "GROUP BY biz_type ORDER BY total DESC LIMIT 20" +
            "</script>")
    List<ApStatisticsVo.BizTypeStatItem> bizTypeStats(@Param("bizModule") String bizModule);
}
