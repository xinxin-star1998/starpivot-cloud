package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.MemberPriceReqBo;
import cn.org.starpivot.mall.sms.domain.bo.MemberPriceSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.MemberPriceVo;
import java.util.List;

/**
 * Memberpriceservice服务接口。
 * <p>
 * 封装会员价格相关业务逻辑。
 * </p>
 */

public interface SmsMemberPriceService {

    /**
     * 分页查询列表。
     */
    PageResponse<MemberPriceVo> pageList(MemberPriceReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    MemberPriceVo getById(Long id);

    /**
     * 新增记录。
     */
    void add(MemberPriceSaveBo bo);

    /**
     * 修改记录。
     */
    void update(MemberPriceSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);
}
