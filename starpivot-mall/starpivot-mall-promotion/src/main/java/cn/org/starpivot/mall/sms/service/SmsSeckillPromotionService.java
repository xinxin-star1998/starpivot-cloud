package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SeckillPromotionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SeckillPromotionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SeckillPromotionVo;

import java.util.List;

/**
 * Seckillpromotionservice服务接口。
 * <p>
 * 封装秒杀活动相关业务逻辑。
 * </p>
 */

public interface SmsSeckillPromotionService {

    /**
     * 分页查询列表。
     */
    PageResponse<SeckillPromotionVo> pageList(SeckillPromotionReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    SeckillPromotionVo getById(Long id);

    /**
     * 新增记录。
     */
    void add(SeckillPromotionSaveBo bo);

    /**
     * 修改记录。
     */
    void update(SeckillPromotionSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
