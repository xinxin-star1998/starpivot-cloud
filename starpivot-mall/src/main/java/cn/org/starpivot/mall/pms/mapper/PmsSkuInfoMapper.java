package cn.org.starpivot.mall.pms.mapper;

import cn.org.starpivot.mall.pms.domain.bo.SkuReqBo;
import cn.org.starpivot.mall.pms.domain.vo.SkuVo;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PmsSkuInfoMapper extends BaseMapper<PmsSkuInfo> {

    IPage<SkuVo> selectPageList(Page<SkuVo> page, @Param("req") SkuReqBo reqBo);

    /** 支付成功后累加 SKU 销量 */
    int incrementSaleCount(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    /** 关单/退货回滚 SKU 销量 */
    int decrementSaleCount(@Param("skuId") Long skuId, @Param("quantity") int quantity);
}
