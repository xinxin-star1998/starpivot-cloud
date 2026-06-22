package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.UserPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-岗位关联 Mapper 接口。
 * <p>管理 {@link UserPost} 关联表的批量插入。</p>
 */
@Mapper
public interface UserPostMapper extends BaseMapper<UserPost> {
    /** 批量插入用户与岗位的关联记录。 */
    int insertBatchUserPosts(@Param("list") List<UserPost> list);
}
