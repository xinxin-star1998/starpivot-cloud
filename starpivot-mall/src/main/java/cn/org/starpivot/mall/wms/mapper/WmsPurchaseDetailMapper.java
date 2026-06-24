package cn.org.starpivot.mall.wms.mapper;

import cn.org.starpivot.mall.wms.domain.bo.PurchaseDetailReqBo;
import cn.org.starpivot.mall.wms.entity.WmsPurchaseDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WmsPurchaseDetailMapper extends BaseMapper<WmsPurchaseDetail> {

    IPage<WmsPurchaseDetail> selectPageList(Page<WmsPurchaseDetail> page, @Param("query") PurchaseDetailReqBo query);

    List<WmsPurchaseDetail> listByPurchaseId(@Param("purchaseId") Long purchaseId);
}
