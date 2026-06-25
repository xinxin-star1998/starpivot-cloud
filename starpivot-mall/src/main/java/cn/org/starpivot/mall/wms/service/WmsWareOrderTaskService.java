package cn.org.starpivot.mall.wms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.wms.domain.bo.WareOrderTaskReqBo;
import cn.org.starpivot.mall.wms.domain.vo.WareOrderTaskVo;
import cn.org.starpivot.mall.wms.entity.WmsWareOrderTask;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Wareordertaskservice服务接口。
 * <p>
 * 封装仓库工单相关业务逻辑。
 * </p>
 */

public interface WmsWareOrderTaskService extends IService<WmsWareOrderTask> {

    /**
     * 分页查询列表。
     */
    PageResponse<WareOrderTaskVo> pageList(WareOrderTaskReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    WareOrderTaskVo getDetailById(Long id);

    /**
     * lockStock。
     */
    void lockStock(Long taskId);

    /**
     * deductStock。
     */
    void deductStock(Long taskId);

    /**
     * unlockStock。
     */
    void unlockStock(Long taskId);

    /**
     * createFromOrder。
     */
    Long createFromOrder(Long orderId);
}
