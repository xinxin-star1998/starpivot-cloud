package cn.org.starpivot.mall.wms.mapper;

import cn.org.starpivot.mall.wms.domain.dto.AddressQueryDTO;
import cn.org.starpivot.mall.wms.entity.Address;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * addressMapper接口
 * 
 * @author admin
 * @since 2026-05-19
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address>
{
    /**
     * 分页查询address列表
     * 
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<Address> selectPageList(Page<Address> page, @Param("queryDTO") AddressQueryDTO queryDTO);

    /** 列表查询（不分页，慎用） */
    List<Address> selectList(@Param("queryDTO") AddressQueryDTO queryDTO);

    /** 按父级编码查询直接子节点（懒加载） */
    List<Address> selectByParentCode(@Param("parentCode") String parentCode);

    /** 条件搜索（最多 200 条） */
    List<Address> searchList(@Param("queryDTO") AddressQueryDTO queryDTO);
}
