package cn.org.starpivot.mall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.wms.domain.dto.WmsWareSkuDTO;
import cn.org.starpivot.mall.wms.domain.dto.WmsWareSkuQueryDTO;
import cn.org.starpivot.mall.wms.entity.WmsWareSku;
import cn.org.starpivot.mall.wms.domain.vo.WmsWareSkuVO;

/**
 * 商品库存Service接口
 * 
 * @author admin
 * @since 2026-05-22
 */
public interface WmsWareSkuService extends IService<WmsWareSku>
{
    /**
     * 分页查询商品库存列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResponse<WmsWareSkuVO> selectWmsWareSkuPage(WmsWareSkuQueryDTO queryDTO);

    /**
     * 根据主键查询商品库存详细信息
     * 
     * @param id 商品库存主键
     * @return 商品库存信息
     */
    WmsWareSkuVO selectWmsWareSkuById(Long id);

    /**
     * 新增商品库存
     * 
     * @param wmsWareSkuDTO 商品库存信息
     * @return 是否成功
     */
    boolean insertWmsWareSku(WmsWareSkuDTO wmsWareSkuDTO);

    /**
     * 修改商品库存
     * 
     * @param wmsWareSkuDTO 商品库存信息
     * @return 是否成功
     */
    boolean updateWmsWareSku(WmsWareSkuDTO wmsWareSkuDTO);

    /**
     * 批量删除商品库存
     * 
     * @param ids 需要删除的商品库存主键数组
     * @return 是否成功
     */
    boolean deleteWmsWareSkuByIds(Long[] ids);

    void addStock(Long skuId, Long wareId, Integer skuNum);
}
