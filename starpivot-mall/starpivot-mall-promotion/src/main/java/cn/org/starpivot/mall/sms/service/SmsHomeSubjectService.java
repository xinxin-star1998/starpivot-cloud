package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.HomeSubjectReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeSubjectSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeSubjectVo;

import java.util.List;

/**
 * Homesubjectservice服务接口。
 * <p>
 * 封装首页专题相关业务逻辑。
 * </p>
 */

public interface SmsHomeSubjectService {

    /**
     * 分页查询列表。
     */
    PageResponse<HomeSubjectVo> pageList(HomeSubjectReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    HomeSubjectVo getById(Long id);

    /**
     * 新增记录。
     */
    void add(HomeSubjectSaveBo bo);

    /**
     * 修改记录。
     */
    void update(HomeSubjectSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
