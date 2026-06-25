package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SkuLadderReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SkuLadderSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SkuLadderVo;
import java.util.List;

/**
 * Skuladderservice服务接口。
 * <p>
 * 封装SKU 阶梯价相关业务逻辑。
 * </p>
 */

public interface SmsSkuLadderService {

    /**
     * 分页查询列表。
     */
    PageResponse<SkuLadderVo> pageList(SkuLadderReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    SkuLadderVo getById(Long id);

    /**
     * 新增记录。
     */
    void add(SkuLadderSaveBo bo);

    /**
     * 修改记录。
     */
    void update(SkuLadderSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
