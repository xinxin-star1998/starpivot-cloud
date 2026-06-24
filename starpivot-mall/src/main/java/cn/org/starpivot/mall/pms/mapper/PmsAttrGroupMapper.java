package cn.org.starpivot.mall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.org.starpivot.mall.pms.domain.dto.PmsAttrGroupQueryDTO;
import cn.org.starpivot.mall.pms.entity.PmsAttrGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组Mapper接口
 * 
 * @author admin
 * @since 2026-05-18
 */
@Mapper
public interface PmsAttrGroupMapper extends BaseMapper<PmsAttrGroup>
{
    /**
     * 分页查询属性分组列表
     * 
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<PmsAttrGroup> selectPageList(Page<PmsAttrGroup> page, @Param("queryDTO" ) PmsAttrGroupQueryDTO queryDTO);

    /** 按条件查询列表（导出用，不分页）；勿命名为 selectList，会与 BaseMapper#selectList(Wrapper) 冲突 */
    List<PmsAttrGroup> selectListByQuery(@Param("queryDTO") PmsAttrGroupQueryDTO queryDTO);
}
