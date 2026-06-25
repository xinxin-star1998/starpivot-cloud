package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.mall.oms.entity.OmsOrderSetting;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Ordersettingservice服务接口。
 * <p>
 * 封装订单设置相关业务逻辑。
 * </p>
 */

public interface OmsOrderSettingService extends IService<OmsOrderSetting> {

    /**
     * 获取Setting。
     */
    OmsOrderSetting getSetting();

    /**
     * 修改记录。
     */
    void updateSetting(OmsOrderSetting setting);
}
