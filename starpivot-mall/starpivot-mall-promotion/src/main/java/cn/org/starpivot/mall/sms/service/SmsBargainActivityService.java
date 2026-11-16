package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.BargainActivityReqBo;
import cn.org.starpivot.mall.sms.domain.bo.BargainActivitySaveBo;
import cn.org.starpivot.mall.sms.domain.vo.BargainActivityVo;

import java.util.List;

/**
 * 砍价活动后台管理服务。
 */
public interface SmsBargainActivityService {

    /**
     * 分页查询砍价活动。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页结果
     */
    PageResponse<BargainActivityVo> pageList(BargainActivityReqBo reqBo);

    /**
     * 查询砍价活动详情。
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    BargainActivityVo getById(Long id);

    /**
     * 新增砍价活动。
     *
     * @param bo 保存参数
     */
    void add(BargainActivitySaveBo bo);

    /**
     * 修改砍价活动。
     *
     * @param bo 保存参数
     */
    void update(BargainActivitySaveBo bo);

    /**
     * 批量删除砍价活动。
     *
     * @param ids 活动 ID 列表
     */
    void removeByIds(List<Long> ids);
}
