package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.HomeAdvReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeAdvSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeAdvVo;
import java.util.List;

/**
 * Homeadvservice服务接口。
 * <p>
 * 封装首页广告相关业务逻辑。
 * </p>
 */

public interface SmsHomeAdvService {

    /**
     * 分页查询列表。
     */
    PageResponse<HomeAdvVo> pageList(HomeAdvReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    HomeAdvVo getById(Long id);

    /**
     * 新增记录。
     */
    void add(HomeAdvSaveBo bo);

    /**
     * 修改记录。
     */
    void update(HomeAdvSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
