package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.api.system.dto.AssigneeResolveRequest;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.entity.*;
import cn.org.starpivot.system.mapper.*;
import cn.org.starpivot.system.service.OrgAssigneeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrgAssigneeServiceImpl implements OrgAssigneeService {

    public static final String ASSIGNEE_STARTER = "STARTER";
    public static final String ASSIGNEE_DEPT_LEADER = "DEPT_LEADER";
    public static final String ASSIGNEE_ROLE = "ROLE";
    public static final String ASSIGNEE_POST = "POST";
    public static final String ASSIGNEE_USER = "USER";

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PostMapper postMapper;
    private final UserPostMapper userPostMapper;

    @Override
    public List<Long> resolveAssignees(AssigneeResolveRequest request) {
        if (request == null || !StringUtils.hasText(request.getAssigneeType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "审批人策略不能为空");
        }
        return switch (request.getAssigneeType()) {
            case ASSIGNEE_STARTER -> List.of(requireStarter(request.getStarterId()));
            case ASSIGNEE_DEPT_LEADER -> resolveDeptLeader(request.getStarterId());
            case ASSIGNEE_ROLE -> resolveByRole(request.getAssigneeValue());
            case ASSIGNEE_POST -> resolveByPost(request.getAssigneeValue());
            case ASSIGNEE_USER -> List.of(parseUserId(request.getAssigneeValue()));
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "不支持的审批人策略: " + request.getAssigneeType());
        };
    }

    @Override
    public String displayName(Long userId) {
        if (userId == null) {
            return "";
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return String.valueOf(userId);
        }
        return StringUtils.hasText(user.getNickName()) ? user.getNickName() : user.getUserName();
    }

    private Long requireStarter(Long starterId) {
        if (starterId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "发起人不能为空");
        }
        return starterId;
    }

    private Long parseUserId(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "指定用户 ID 不能为空");
        }
        return Long.valueOf(value.trim());
    }

    private List<Long> resolveDeptLeader(Long starterId) {
        SysUser starter = userMapper.selectById(starterId);
        if (starter == null || starter.getDeptId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "发起人未关联部门，无法解析部门负责人");
        }
        SysDept dept = deptMapper.selectById(starter.getDeptId());
        if (dept == null || !StringUtils.hasText(dept.getLeader())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "部门未配置负责人");
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeptId, dept.getDeptId())
                .and(w -> w.eq(SysUser::getUserName, dept.getLeader())
                        .or()
                        .eq(SysUser::getNickName, dept.getLeader()))
                .eq(SysUser::getStatus, AppConstants.Status.NORMAL)
                .last("LIMIT 1");
        SysUser leader = userMapper.selectOne(wrapper);
        if (leader == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "未找到部门负责人对应用户: " + dept.getLeader());
        }
        return List.of(leader.getUserId());
    }

    private List<Long> resolveByRole(String roleKey) {
        if (!StringUtils.hasText(roleKey)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "角色标识不能为空");
        }
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRole::getRoleKey, roleKey).eq(SysRole::getStatus, AppConstants.Status.NORMAL);
        SysRole role = roleMapper.selectOne(roleWrapper);
        if (role == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "角色不存在: " + roleKey);
        }
        LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(UserRole::getRoleId, role.getRoleId());
        List<UserRole> userRoles = userRoleMapper.selectList(urWrapper);
        if (CollectionUtils.isEmpty(userRoles)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "角色下无用户: " + roleKey);
        }
        return activeUserIds(userRoles.stream().map(UserRole::getUserId).distinct().toList(),
                "角色下无可用用户: " + roleKey);
    }

    private List<Long> resolveByPost(String postCode) {
        if (!StringUtils.hasText(postCode)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "岗位编码不能为空");
        }
        LambdaQueryWrapper<SysPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(SysPost::getPostCode, postCode).eq(SysPost::getStatus, AppConstants.Status.NORMAL);
        SysPost post = postMapper.selectOne(postWrapper);
        if (post == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "岗位不存在: " + postCode);
        }
        LambdaQueryWrapper<UserPost> upWrapper = new LambdaQueryWrapper<>();
        upWrapper.eq(UserPost::getPostId, post.getPostId());
        List<UserPost> userPosts = userPostMapper.selectList(upWrapper);
        if (CollectionUtils.isEmpty(userPosts)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "岗位下无用户: " + postCode);
        }
        return activeUserIds(userPosts.stream().map(UserPost::getUserId).distinct().toList(),
                "岗位下无可用用户: " + postCode);
    }

    private List<Long> activeUserIds(List<Long> userIds, String emptyMessage) {
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        Set<Long> active = users.stream()
                .filter(user -> AppConstants.Status.NORMAL.equals(user.getStatus()))
                .map(SysUser::getUserId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (active.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, emptyMessage);
        }
        return new ArrayList<>(active);
    }
}
