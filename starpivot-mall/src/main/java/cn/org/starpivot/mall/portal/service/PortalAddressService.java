package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalAddressSaveBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalAddressVo;

import java.util.List;

/**
 * Addressservice服务接口。
 * <p>
 * 封装地址相关业务逻辑。
 * </p>
 */

public interface PortalAddressService {

    /**
     * 查询列表。
     */
    List<PortalAddressVo> listByMember(Long memberId);

    /**
     * 根据 ID 获取详情。
     */
    PortalAddressVo getById(Long memberId, Long addressId);

    /**
     * 保存记录。
     */
    void save(Long memberId, PortalAddressSaveBo bo);

    /**
     * 删除记录。
     */
    void remove(Long memberId, Long addressId);
}
