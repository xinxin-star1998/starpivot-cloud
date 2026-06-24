package cn.org.starpivot.mall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.org.starpivot.mall.pms.domain.bo.SkuReqBo;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.domain.vo.SkuVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PmsSkuInfoMapper extends BaseMapper<PmsSkuInfo> {

    IPage<SkuVo> selectPageList(Page<SkuVo> page, @Param("req") SkuReqBo reqBo);
}
