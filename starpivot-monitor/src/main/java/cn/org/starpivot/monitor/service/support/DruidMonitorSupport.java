package cn.org.starpivot.monitor.service.support;

import cn.org.starpivot.monitor.domain.vo.DruidMonitorVO;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidStatManagerFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.org.starpivot.common.util.LogUtils.truncateString;

/**
 * Druid 连接池与 SQL 统计监控。
 */
@Slf4j
@Component
public class DruidMonitorSupport {

    private static final double PERCENTAGE_BASE = 100.0;

    @Autowired(required = false)
    private DataSource dataSource;

    public DruidMonitorVO collectDruidMonitorInfo() {
        return collectDruidMonitorInfo(false, null);
    }

    public DruidMonitorVO collectDruidMonitorInfoWithSlowSql(Long slowSqlThreshold) {
        return collectDruidMonitorInfo(true, slowSqlThreshold);
    }

    public DruidMonitorVO collectDruidMonitorInfo(boolean includeSlowSqlList, Long slowSqlThreshold) {
        if (dataSource == null || !(dataSource instanceof DruidDataSource)) {
            DruidMonitorVO vo = new DruidMonitorVO();
            vo.setAvailable(false);
            vo.setMessage("数据源不是 Druid 数据源或数据源未配置");
            return vo;
        }

        try {
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;
            DruidMonitorVO monitorVO = new DruidMonitorVO();
            monitorVO.setName(druidDataSource.getName());
            monitorVO.setDbType(druidDataSource.getDbType());
            monitorVO.setDriverClassName(druidDataSource.getDriverClassName());

            DruidMonitorVO.ConnectionPoolInfo poolInfo = new DruidMonitorVO.ConnectionPoolInfo();
            poolInfo.setInitialSize(druidDataSource.getInitialSize());
            poolInfo.setMinIdle(druidDataSource.getMinIdle());
            poolInfo.setMaxActive(druidDataSource.getMaxActive());
            poolInfo.setActiveCount(druidDataSource.getActiveCount());
            poolInfo.setActivePeak(druidDataSource.getActivePeak());
            if (druidDataSource.getMaxActive() > 0) {
                poolInfo.setUsage(PERCENTAGE_BASE * druidDataSource.getActiveCount() / druidDataSource.getMaxActive());
            } else {
                poolInfo.setUsage(0.0);
            }
            monitorVO.setConnectionPool(poolInfo);

            DruidStatManagerFacade statManagerFacade = DruidStatManagerFacade.getInstance();
            List<Map<String, Object>> sqlList = statManagerFacade.getSqlStatDataList(null);

            DruidMonitorVO.SqlStatInfo sqlStatInfo = new DruidMonitorVO.SqlStatInfo();
            long executeCount = 0;
            long executeMillisTotal = 0;
            long slowSqlCount = 0;
            long errorSqlCount = 0;

            List<DruidMonitorVO.SlowSqlInfo> slowSqlList = null;
            if (includeSlowSqlList) {
                slowSqlList = new ArrayList<>();
                long threshold = (slowSqlThreshold != null && slowSqlThreshold > 0) ? slowSqlThreshold : 5000L;

                if (sqlList != null) {
                    for (Map<String, Object> sql : sqlList) {
                        try {
                            String sqlId = getStringValue(sql, "ID");
                            String sqlText = getStringValue(sql, "SQL");
                            Long count = getLongValue(sql, "ExecuteCount");
                            Long millis = getLongValue(sql, "ExecuteMillisTotal");
                            Long maxMillis = getLongValue(sql, "ExecuteMillisMax");
                            Long slowCount = getLongValue(sql, "SlowCount");
                            Long errorCount = getLongValue(sql, "ErrorCount");
                            Long lastExecuteTime = getLongValue(sql, "LastExecuteTime");

                            if (count != null) executeCount += count;
                            if (millis != null) executeMillisTotal += millis;
                            if (slowCount != null) slowSqlCount += slowCount;
                            if (errorCount != null) errorSqlCount += errorCount;

                            if (count != null && count > 0 && millis != null) {
                                double avgTime = (double) millis / count;
                                if (avgTime >= threshold || (slowCount != null && slowCount > 0)) {
                                    DruidMonitorVO.SlowSqlInfo slowSqlInfo = new DruidMonitorVO.SlowSqlInfo();
                                    slowSqlInfo.setSqlId(sqlId);
                                    slowSqlInfo.setSqlText(sqlText != null ? truncateString(sqlText, 5000) : "");
                                    slowSqlInfo.setExecuteCount(count);
                                    slowSqlInfo.setExecuteTimeTotal(millis);
                                    slowSqlInfo.setExecuteTimeMax(maxMillis);
                                    slowSqlInfo.setExecuteTimeAvg(avgTime);
                                    slowSqlInfo.setSlowCount(slowCount != null ? slowCount : 0L);
                                    slowSqlInfo.setErrorCount(errorCount != null ? errorCount : 0L);
                                    slowSqlInfo.setLastExecuteTime(lastExecuteTime);
                                    slowSqlList.add(slowSqlInfo);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("处理慢SQL信息失败", e);
                        }
                    }
                }
            } else if (sqlList != null) {
                for (Map<String, Object> sql : sqlList) {
                    Long count = getLongValue(sql, "ExecuteCount");
                    Long millis = getLongValue(sql, "ExecuteMillisTotal");
                    Long slowCount = getLongValue(sql, "SlowCount");
                    Long errorCount = getLongValue(sql, "ErrorCount");

                    if (count != null) executeCount += count;
                    if (millis != null) executeMillisTotal += millis;
                    if (slowCount != null) slowSqlCount += slowCount;
                    if (errorCount != null) errorSqlCount += errorCount;
                }
            }

            sqlStatInfo.setExecuteCount(executeCount);
            sqlStatInfo.setExecuteMillisTotal(executeMillisTotal);
            sqlStatInfo.setExecuteMillisAvg(executeCount > 0 ? (double) executeMillisTotal / executeCount : 0.0);
            sqlStatInfo.setSlowSqlCount(slowSqlCount);
            sqlStatInfo.setErrorSqlCount(errorSqlCount);
            monitorVO.setSqlStat(sqlStatInfo);
            monitorVO.setSlowSqlList(slowSqlList);
            monitorVO.setAvailable(true);
            return monitorVO;
        } catch (Exception e) {
            log.error("获取 Druid 监控信息失败", e);
            DruidMonitorVO vo = new DruidMonitorVO();
            vo.setAvailable(false);
            vo.setMessage("获取 Druid 监控信息失败: " + e.getMessage());
            return vo;
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
