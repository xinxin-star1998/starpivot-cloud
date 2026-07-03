package cn.org.starpivot.mall.pms.mapper;

import cn.org.starpivot.mall.pms.domain.dto.PmsAttrQueryDTO;
import cn.org.starpivot.mall.pms.entity.PmsAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性Mapper接口
 * 
 * @author admin
 * @since 2026-05-18
 */
@Mapper
public interface PmsAttrMapper extends BaseMapper<PmsAttr>
{
    /**
     * 分页查询商品属性列表
     * 
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<PmsAttr> selectPageList(Page<PmsAttr> page, @Param("queryDTO" ) PmsAttrQueryDTO queryDTO);

    /** 按条件查询列表（导出用，不分页）；勿命名为 selectList，会与 BaseMapper#selectList(Wrapper) 冲突 */
    List<PmsAttr> selectListByQuery(@Param("queryDTO") PmsAttrQueryDTO queryDTO);
}
