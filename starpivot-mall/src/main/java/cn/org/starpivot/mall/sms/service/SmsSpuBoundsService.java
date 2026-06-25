package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SpuBoundsReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SpuBoundsSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SpuBoundsVo;
import java.util.List;

/**
 * Spuboundsservice服务接口。
 * <p>
 * 封装SPU 积分/成长值相关业务逻辑。
 * </p>
 */

public interface SmsSpuBoundsService {

    /**
     * 分页查询列表。
     */
    PageResponse<SpuBoundsVo> pageList(SpuBoundsReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    SpuBoundsVo getById(Long id);

    /**
     * 获取BySpuId。
     */
    SpuBoundsVo getBySpuId(Long spuId);

    /**
     * 新增记录。
     */
    void add(SpuBoundsSaveBo bo);

    /**
     * 修改记录。
     */
    void update(SpuBoundsSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
