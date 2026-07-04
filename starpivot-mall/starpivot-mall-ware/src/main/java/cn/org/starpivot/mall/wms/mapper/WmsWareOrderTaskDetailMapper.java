package cn.org.starpivot.mall.wms.mapper;

import cn.org.starpivot.mall.wms.entity.WmsWareOrderTaskDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WmsWareOrderTaskDetailMapper extends BaseMapper<WmsWareOrderTaskDetail> {

    List<WmsWareOrderTaskDetail> listByTaskId(@Param("taskId") Long taskId);
}
