package cn.org.starpivot.system.service.support;

import cn.org.starpivot.system.domain.entity.UserPost;
import cn.org.starpivot.system.domain.entity.UserRole;
import cn.org.starpivot.system.mapper.UserPostMapper;
import cn.org.starpivot.system.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户与角色、岗位关联写入。
 */
@Component
@RequiredArgsConstructor
public class SysUserRelationSupport {

    private final UserRoleMapper userRoleMapper;
    private final UserPostMapper userPostMapper;

    public void insertUserRoles(Long userId, List<Long> roleIds) {
        List<UserRole> userRoles = new ArrayList<>(roleIds.size());
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoles.add(userRole);
        }
        userRoleMapper.insertBatchUserRoles(userRoles);
    }

    public void insertUserPosts(Long userId, List<Long> postIds) {
        List<UserPost> userPosts = new ArrayList<>(postIds.size());
        for (Long postId : postIds) {
            UserPost userPost = new UserPost();
            userPost.setUserId(userId);
            userPost.setPostId(postId);
            userPosts.add(userPost);
        }
        userPostMapper.insertBatchUserPosts(userPosts);
    }
}
