package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SkuFullReductionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SkuFullReductionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SkuFullReductionVo;
import java.util.List;

/**
 * Skufullreductionservice服务接口。
 * <p>
 * 封装SKU 满减相关业务逻辑。
 * </p>
 */

public interface SmsSkuFullReductionService {

    /**
     * 分页查询列表。
     */
    PageResponse<SkuFullReductionVo> pageList(SkuFullReductionReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    SkuFullReductionVo getById(Long id);

    /**
     * 新增记录。
     */
    void add(SkuFullReductionSaveBo bo);

    /**
     * 修改记录。
     */
    void update(SkuFullReductionSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
