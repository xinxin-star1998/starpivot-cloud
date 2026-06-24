package cn.org.starpivot.mall.wms.mapper;

import cn.org.starpivot.mall.wms.entity.WmsWareOrderTaskDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WmsWareOrderTaskDetailMapper extends BaseMapper<WmsWareOrderTaskDetail> {

    List<WmsWareOrderTaskDetail> listByTaskId(@Param("taskId") Long taskId);
}
