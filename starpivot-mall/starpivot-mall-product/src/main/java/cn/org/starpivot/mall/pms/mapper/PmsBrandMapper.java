package cn.org.starpivot.mall.pms.mapper;

import cn.org.starpivot.mall.pms.domain.bo.BrandReqBo;
import cn.org.starpivot.mall.pms.entity.PmsBrand;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PmsBrandMapper extends BaseMapper<PmsBrand> {

    IPage<PmsBrand> selectPageList(Page<PmsBrand> page, @Param("brandReqBo") BrandReqBo brandReqBo);
}
