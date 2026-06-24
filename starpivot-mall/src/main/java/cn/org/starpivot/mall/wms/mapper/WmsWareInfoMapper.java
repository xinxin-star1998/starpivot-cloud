package cn.org.starpivot.mall.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.org.starpivot.mall.wms.domain.bo.WmsWareInfoReqBo;
import cn.org.starpivot.mall.wms.entity.WmsWareInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WmsWareInfoMapper extends BaseMapper<WmsWareInfo> {
    IPage<WmsWareInfo> selectPageList(
            Page<WmsWareInfo> page, @Param("queryDTO") WmsWareInfoReqBo wmsWareInfoReqBo);
}
