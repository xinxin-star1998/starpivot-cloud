package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.mall.oms.entity.OmsOrderSetting;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OmsOrderSettingService extends IService<OmsOrderSetting> {

    OmsOrderSetting getSetting();

    void updateSetting(OmsOrderSetting setting);
}
