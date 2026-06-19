package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<SysPost> {
}
