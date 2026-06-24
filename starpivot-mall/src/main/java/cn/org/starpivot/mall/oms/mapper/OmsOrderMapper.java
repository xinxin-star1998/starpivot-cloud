package cn.org.starpivot.mall.oms.mapper;

import cn.org.starpivot.mall.oms.domain.bo.OmsOrderReqBo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OmsOrderMapper extends BaseMapper<OmsOrder> {

    IPage<OmsOrder> selectPageList(Page<OmsOrder> page, @Param("query") OmsOrderReqBo query);
}
