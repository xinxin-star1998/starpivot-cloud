package cn.org.starpivot.mall.wms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.wms.domain.bo.WareOrderTaskReqBo;
import cn.org.starpivot.mall.wms.domain.vo.WareOrderTaskVo;
import cn.org.starpivot.mall.wms.entity.WmsWareOrderTask;
import com.baomidou.mybatisplus.extension.service.IService;

public interface WmsWareOrderTaskService extends IService<WmsWareOrderTask> {

    PageResponse<WareOrderTaskVo> pageList(WareOrderTaskReqBo reqBo);

    WareOrderTaskVo getDetailById(Long id);

    void lockStock(Long taskId);

    void deductStock(Long taskId);

    void unlockStock(Long taskId);

    Long createFromOrder(Long orderId);
}
