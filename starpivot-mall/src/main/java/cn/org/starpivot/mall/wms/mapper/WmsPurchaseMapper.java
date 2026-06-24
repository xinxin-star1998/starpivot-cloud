package cn.org.starpivot.mall.wms.mapper;

import cn.org.starpivot.mall.wms.domain.bo.PurchaseReqBo;
import cn.org.starpivot.mall.wms.entity.WmsPurchase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WmsPurchaseMapper extends BaseMapper<WmsPurchase> {

    IPage<WmsPurchase> selectPageList(Page<WmsPurchase> page, @Param("query") PurchaseReqBo query);

    IPage<WmsPurchase> selectUnreceivePage(Page<WmsPurchase> page, @Param("query") PurchaseReqBo query);
}
