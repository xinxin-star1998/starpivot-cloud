package cn.org.starpivot.mall.oms.mapper;

import cn.org.starpivot.mall.oms.domain.bo.ReturnReqBo;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OmsOrderReturnApplyMapper extends BaseMapper<OmsOrderReturnApply> {

    IPage<OmsOrderReturnApply> selectPageList(Page<OmsOrderReturnApply> page, @Param("query") ReturnReqBo query);
}
