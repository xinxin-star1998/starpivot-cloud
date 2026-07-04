package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.mall.ums.domain.bo.MemberLevelSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberLevelVo;

import java.util.List;

/**
 * Memberlevelservice服务接口。
 * <p>
 * 封装会员等级相关业务逻辑。
 * </p>
 */

public interface UmsMemberLevelService {

    /**
     * 查询列表。
     */
    List<MemberLevelVo> listAll();

    /**
     * 根据 ID 获取详情。
     */
    MemberLevelVo getById(Long id);

    /**
     * 新增记录。
     */
    void add(MemberLevelSaveBo bo);

    /**
     * 修改记录。
     */
    void update(MemberLevelSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
