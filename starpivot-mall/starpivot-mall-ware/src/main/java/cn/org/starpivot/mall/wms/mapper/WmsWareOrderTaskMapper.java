package cn.org.starpivot.mall.wms.mapper;

import cn.org.starpivot.mall.wms.domain.bo.WareOrderTaskReqBo;
import cn.org.starpivot.mall.wms.entity.WmsWareOrderTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WmsWareOrderTaskMapper extends BaseMapper<WmsWareOrderTask> {

    IPage<WmsWareOrderTask> selectPageList(Page<WmsWareOrderTask> page, @Param("query") WareOrderTaskReqBo query);
}
