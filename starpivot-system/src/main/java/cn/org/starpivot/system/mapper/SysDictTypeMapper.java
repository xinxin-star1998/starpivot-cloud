package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysDictType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典类型 Mapper 接口。
 * <p>继承 {@link BaseMapper} 提供字典类型表基础 CRUD。</p>
 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {
}