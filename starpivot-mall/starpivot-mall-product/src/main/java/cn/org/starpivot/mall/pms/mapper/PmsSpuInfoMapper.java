package cn.org.starpivot.mall.pms.mapper;

import cn.org.starpivot.mall.pms.domain.bo.ProductReqBo;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PmsSpuInfoMapper extends BaseMapper<PmsSpuInfo> {

    IPage<PmsSpuInfo> selectPageList(Page<PmsSpuInfo> page, ProductReqBo productReqBo);
}
