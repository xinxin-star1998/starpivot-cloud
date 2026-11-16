package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.GroupBuyActivityReqBo;
import cn.org.starpivot.mall.sms.domain.bo.GroupBuyActivitySaveBo;
import cn.org.starpivot.mall.sms.domain.vo.GroupBuyActivityVo;

import java.util.List;

/**
 * 拼团活动后台管理服务。
 */
public interface SmsGroupBuyActivityService {

    /**
     * 分页查询拼团活动。
     *
     * @param reqBo 分页及筛选条件
     * @return 分页结果
     */
    PageResponse<GroupBuyActivityVo> pageList(GroupBuyActivityReqBo reqBo);

    /**
     * 查询拼团活动详情。
     *
     * @param id 活动 ID
     * @return 活动详情
     */
    GroupBuyActivityVo getById(Long id);

    /**
     * 新增拼团活动。
     *
     * @param bo 保存参数
     */
    void add(GroupBuyActivitySaveBo bo);

    /**
     * 修改拼团活动。
     *
     * @param bo 保存参数
     */
    void update(GroupBuyActivitySaveBo bo);

    /**
     * 批量删除拼团活动。
     *
     * @param ids 活动 ID 列表
     */
    void removeByIds(List<Long> ids);
}
