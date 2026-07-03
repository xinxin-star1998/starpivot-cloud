package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SeckillSessionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SeckillSessionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SeckillSessionVo;

import java.util.List;

/**
 * Seckillsessionservice服务接口。
 * <p>
 * 封装秒杀场次相关业务逻辑。
 * </p>
 */

public interface SmsSeckillSessionService {

    /**
     * 分页查询列表。
     */
    PageResponse<SeckillSessionVo> pageList(SeckillSessionReqBo reqBo);

    /**
     * 查询列表。
     */
    List<SeckillSessionVo> listAll();

    /**
     * 根据 ID 获取详情。
     */
    SeckillSessionVo getById(Long id);

    /**
     * 新增记录。
     */
    void add(SeckillSessionSaveBo bo);

    /**
     * 修改记录。
     */
    void update(SeckillSessionSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
