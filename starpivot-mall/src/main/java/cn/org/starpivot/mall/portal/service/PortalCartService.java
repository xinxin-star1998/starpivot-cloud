package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalCartAddBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCartUpdateBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCartVo;

import java.util.List;




/**
 * Cartservice服务接口。
 * <p>
 * 封装Cart相关业务逻辑。
 * </p>
 */

public interface PortalCartService {

    /**
     * 查询列表。
     */
    PortalCartVo listCart(Long memberId);

    /**
     * 新增记录。
     */
    void addItem(Long memberId, PortalCartAddBo bo);

    /**
     * 修改记录。
     */
    void updateItem(Long memberId, PortalCartUpdateBo bo);

    /**
     * 删除记录。
     */
    void removeItems(Long memberId, List<Long> skuIds);

    /**
     * clearChecked。
     */
    void clearChecked(Long memberId, List<Long> skuIds);
}
