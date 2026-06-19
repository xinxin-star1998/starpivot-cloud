package cn.org.starpivot.system.assembler;

import cn.org.starpivot.system.domain.bo.UserVO;
import cn.org.starpivot.system.domain.entity.*;
import cn.org.starpivot.system.mapper.*;
import cn.org.starpivot.system.service.AccountLockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserVOAssembler {

    @Autowired
    private SysDeptMapper deptMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private UserPostMapper userPostMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired(required = false)
    private AccountLockService accountLockService;

    public List<UserVO> convertToVOList(List<SysUser> userList) {
        if (userList == null || userList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = userList.stream().map(SysUser::getUserId).collect(Collectors.toList());
        List<Long> deptIds = userList.stream().map(SysUser::getDeptId).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        Map<Long, String> deptNameMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            deptMapper.selectList(new LambdaQueryWrapper<SysDept>().in(SysDept::getDeptId, deptIds))
                    .forEach(dept -> deptNameMap.put(dept.getDeptId(), dept.getDeptName()));
        }

        Map<Long, List<SysRole>> userRolesMap = buildUserRolesMap(userIds);
        Map<Long, List<Long>> userPostIdsMap = new HashMap<>();
        Map<Long, String> postNameMap = new HashMap<>();
        buildUserPostMaps(userIds, userPostIdsMap, postNameMap);

        List<UserVO> voList = new ArrayList<>();
        for (SysUser user : userList) {
            voList.add(convertToVO(user, deptNameMap, userRolesMap, userPostIdsMap, postNameMap));
        }
        if (accountLockService != null) {
            for (UserVO vo : voList) {
                vo.setIsLocked(vo.getUserName() != null && accountLockService.isAccountLocked(vo.getUserName()));
            }
        }
        return voList;
    }

    public UserVO convertToVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }

        List<SysRole> roles = sysRoleMapper.selectRoleListByUserId(user.getUserId());
        if (roles != null && !roles.isEmpty()) {
            vo.setRoleIds(roles.stream().map(SysRole::getRoleId).collect(Collectors.toList()));
            vo.setRoleNames(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));
        }

        LambdaQueryWrapper<UserPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(UserPost::getUserId, user.getUserId());
        List<UserPost> userPosts = userPostMapper.selectList(postWrapper);
        if (userPosts != null && !userPosts.isEmpty()) {
            List<Long> postIds = userPosts.stream().map(UserPost::getPostId).collect(Collectors.toList());
            vo.setPostIds(postIds);
            List<SysPost> postList = postMapper.selectList(new LambdaQueryWrapper<SysPost>().in(SysPost::getPostId, postIds));
            Map<Long, String> postNameMap = postList.stream()
                    .collect(Collectors.toMap(SysPost::getPostId, SysPost::getPostName, (a, b) -> a));
            vo.setPostNames(postIds.stream().map(postNameMap::get).filter(Objects::nonNull).collect(Collectors.toList()));
        }

        vo.setIsLocked(accountLockService != null && user.getUserName() != null
                && accountLockService.isAccountLocked(user.getUserName()));
        return vo;
    }

    private Map<Long, List<SysRole>> buildUserRolesMap(List<Long> userIds) {
        Map<Long, List<SysRole>> userRolesMap = new HashMap<>();
        LambdaQueryWrapper<UserRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(UserRole::getUserId, userIds);
        List<UserRole> userRoles = userRoleMapper.selectList(roleWrapper);
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).distinct().collect(Collectors.toList());

        Map<Long, SysRole> roleMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roleIds))
                    .forEach(role -> roleMap.put(role.getRoleId(), role));
        }
        for (UserRole ur : userRoles) {
            SysRole role = roleMap.get(ur.getRoleId());
            if (role != null) {
                userRolesMap.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>()).add(role);
            }
        }
        return userRolesMap;
    }

    private void buildUserPostMaps(List<Long> userIds, Map<Long, List<Long>> userPostIdsMap, Map<Long, String> postNameMap) {
        LambdaQueryWrapper<UserPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.in(UserPost::getUserId, userIds);
        List<UserPost> userPosts = userPostMapper.selectList(postWrapper);
        List<Long> postIds = userPosts.stream().map(UserPost::getPostId).distinct().collect(Collectors.toList());
        if (!postIds.isEmpty()) {
            postMapper.selectList(new LambdaQueryWrapper<SysPost>().in(SysPost::getPostId, postIds))
                    .forEach(post -> postNameMap.put(post.getPostId(), post.getPostName()));
        }
        for (UserPost up : userPosts) {
            userPostIdsMap.computeIfAbsent(up.getUserId(), k -> new ArrayList<>()).add(up.getPostId());
        }
    }

    private UserVO convertToVO(SysUser user, Map<Long, String> deptNameMap, Map<Long, List<SysRole>> userRolesMap,
                                 Map<Long, List<Long>> userPostIdsMap, Map<Long, String> postNameMap) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getDeptId() != null) {
            vo.setDeptName(deptNameMap.get(user.getDeptId()));
        }
        List<SysRole> roles = userRolesMap.getOrDefault(user.getUserId(), List.of());
        if (!roles.isEmpty()) {
            vo.setRoleIds(roles.stream().map(SysRole::getRoleId).collect(Collectors.toList()));
            vo.setRoleNames(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));
        }
        List<Long> postIds = userPostIdsMap.getOrDefault(user.getUserId(), List.of());
        if (!postIds.isEmpty()) {
            vo.setPostIds(postIds);
            vo.setPostNames(postIds.stream().map(postNameMap::get).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return vo;
    }
}
