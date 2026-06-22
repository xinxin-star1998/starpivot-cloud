package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 部门 Mapper 接口。
 * <p>继承 {@link BaseMapper} 提供部门表基础 CRUD 及全量部门 ID 查询。</p>
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /** 查询全部部门 ID 列表。 */
    List<Long> selectAllDeptIds();
}