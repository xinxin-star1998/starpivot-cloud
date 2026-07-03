package cn.org.starpivot.mall.wms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.wms.domain.bo.WmsWareInfoReqBo;
import cn.org.starpivot.mall.wms.domain.bo.WmsWareInfoSaveBo;
import cn.org.starpivot.mall.wms.domain.vo.WmsWareInfoVo;
import cn.org.starpivot.mall.wms.entity.WmsWareInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Wareinfoservice服务接口。
 * <p>
 * 封装仓库相关业务逻辑。
 * </p>
 */

public interface WmsWareInfoService extends IService<WmsWareInfo> {

    /**
     * 获取WmsWareInfoPageList。
     */
    PageResponse<WmsWareInfoVo> getWmsWareInfoPageList(WmsWareInfoReqBo wmsWareInfoReqBo);

    /**
     * 根据 ID 获取详情。
     */
    WmsWareInfoVo getById(Long id);

    /**
     * 新增记录。
     */
    void addWare(WmsWareInfoSaveBo bo);

    /**
     * 修改记录。
     */
    void updateWare(WmsWareInfoSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
