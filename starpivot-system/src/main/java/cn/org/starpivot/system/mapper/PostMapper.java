package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位 Mapper 接口。
 * <p>继承 {@link BaseMapper} 提供岗位表基础 CRUD。</p>
 */
@Mapper
public interface PostMapper extends BaseMapper<SysPost> {
}
