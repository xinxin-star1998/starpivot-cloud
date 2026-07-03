package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.vo.PortalHomeVo;




/**
 * Homeservice服务接口。
 * <p>
 * 封装Home相关业务逻辑。
 * </p>
 */

public interface PortalHomeService {

    /**
     * 获取首页聚合数据。
     */
    PortalHomeVo getHomeData();
}
