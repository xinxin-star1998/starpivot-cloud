package cn.org.starpivot.mall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.wms.domain.bo.WmsWareInfoReqBo;
import cn.org.starpivot.mall.wms.domain.bo.WmsWareInfoSaveBo;
import cn.org.starpivot.mall.wms.entity.WmsWareInfo;
import cn.org.starpivot.mall.wms.domain.vo.WmsWareInfoVo;

import java.util.List;

public interface WmsWareInfoService extends IService<WmsWareInfo> {

    PageResponse<WmsWareInfoVo> getWmsWareInfoPageList(WmsWareInfoReqBo wmsWareInfoReqBo);

    WmsWareInfoVo getById(Long id);

    void addWare(WmsWareInfoSaveBo bo);

    void updateWare(WmsWareInfoSaveBo bo);

    void removeByIds(List<Long> ids);
}
