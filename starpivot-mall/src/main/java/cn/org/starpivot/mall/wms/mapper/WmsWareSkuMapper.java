package cn.org.starpivot.mall.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.org.starpivot.mall.wms.domain.dto.WmsWareSkuQueryDTO;
import cn.org.starpivot.mall.wms.entity.WmsWareSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存Mapper接口
 * 
 * @author admin
 * @since 2026-05-22
 */
@Mapper
public interface WmsWareSkuMapper extends BaseMapper<WmsWareSku>
{
    /**
     * 分页查询商品库存列表
     * 
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<WmsWareSku> selectPageList(Page<WmsWareSku> page, @Param("queryDTO" ) WmsWareSkuQueryDTO queryDTO);

    int addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    int lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    int unlockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    int deductSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    java.util.List<Long> listWareIdHasStock(@Param("skuId") Long skuId);
}
