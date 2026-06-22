package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.vo.ConsoleDashboardVo;

/**
 * 工作台仪表盘服务接口。
 * <p>
 * 聚合控制台首页所需的统计数据。
 * </p>
 */
public interface ConsoleDashboardService {

    /**
     * 获取工作台首页聚合数据。
     *
     * @return 控制台仪表盘视图对象
     */
    ConsoleDashboardVo getConsoleData();
}
