package cn.org.starpivot.mall.ums.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberReqBo;
import cn.org.starpivot.mall.ums.domain.bo.MemberSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberVo;

/**
 * Memberservice服务接口。
 * <p>
 * 封装会员相关业务逻辑。
 * </p>
 */

public interface UmsMemberService {

    /**
     * 分页查询列表。
     */
    PageResponse<MemberVo> pageList(MemberReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    MemberVo getById(Long id);

    /**
     * 修改记录。
     */
    void update(MemberSaveBo bo);
}
