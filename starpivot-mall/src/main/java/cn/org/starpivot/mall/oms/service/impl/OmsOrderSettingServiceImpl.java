package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.entity.OmsOrderSetting;
import cn.org.starpivot.mall.oms.mapper.OmsOrderSettingMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderSettingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OmsOrderSettingServiceImpl extends ServiceImpl<OmsOrderSettingMapper, OmsOrderSetting>
        implements OmsOrderSettingService {

    private static final long DEFAULT_SETTING_ID = 1L;

    @Override
    @Transactional(readOnly = true)
    public OmsOrderSetting getSetting() {
        OmsOrderSetting setting = baseMapper.selectById(DEFAULT_SETTING_ID);
        if (setting == null) {
            throw new BizException("订单设置不存在，请先初始化 oms_order_setting");
        }
        return setting;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetting(OmsOrderSetting setting) {
        if (setting == null) {
            throw new BizException("订单设置不能为空");
        }
        setting.setId(DEFAULT_SETTING_ID);
        OmsOrderSetting existing = baseMapper.selectById(DEFAULT_SETTING_ID);
        if (existing == null) {
            baseMapper.insert(setting);
        } else {
            baseMapper.updateById(setting);
        }
    }
}
