package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.UserPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPostMapper extends BaseMapper<UserPost> {
    int insertBatchUserPosts(@Param("list") List<UserPost> list);
}
