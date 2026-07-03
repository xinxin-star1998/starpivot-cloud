package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.system.domain.bo.DeptVO;
import cn.org.starpivot.system.domain.dto.DeptDTO;
import cn.org.starpivot.system.domain.entity.SysDept;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.mapper.SysDeptMapper;
import cn.org.starpivot.system.mapper.SysUserMapper;
import cn.org.starpivot.system.service.SysDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门管理服务实现类。
 * <p>
 * 实现 {@link SysDeptService}，含部门树构建、增删改及子部门/用户关联校验。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private final SysUserMapper userMapper;

    /**
     * 查询部门树形结构。
     * <p>{@code @Transactional(readOnly = true)} 只读事务；{@code @Cacheable} 结果缓存至 {@link CacheConstants#DEPT_TREE}。</p>
     *
     * @return 按排序号排列的 {@link DeptVO} 树
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConstants.DEPT_TREE, key = "'all'")
    public List<DeptVO> selectDeptTree() {
        List<SysDept> depts = this.list(new LambdaQueryWrapper<SysDept>().eq(SysDept::getDelFlag, "0"));
        return buildDeptTree(depts, 0L);
    }

    /**
     * 根据部门 ID 查询部门详情。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param deptId 部门 ID
     * @return 部门视图对象
     * @throws BizException 部门不存在或已删除时抛出
     */
    @Override
    @Transactional(readOnly = true)
    public DeptVO selectDeptById(Long deptId) {
        SysDept dept = this.getById(deptId);
        AssertUtils.notNull(dept, ErrorCode.DEPT_NOT_FOUND);
        if ("2".equals(dept.getDelFlag())) {
            throw new BizException(ErrorCode.DEPT_NOT_FOUND, "部门不存在");
        }
        DeptVO vo = new DeptVO();
        BeanUtils.copyProperties(dept, vo);
        return vo;
    }

    /**
     * 新增部门，自动维护祖级列表与默认值。
     * <p>{@code @CacheEvict} 清空部门树缓存；{@code @Transactional} 异常时回滚。</p>
     *
     * @param deptDTO 部门信息
     * @return 是否保存成功
     * @throws BizException 同级部门名称已存在时抛出
     */
    @Override
    @CacheEvict(cacheNames = CacheConstants.DEPT_TREE, key = "'all'")
    @Transactional(rollbackFor = Exception.class)
    public boolean insertDept(DeptDTO deptDTO) {
        if (!checkDeptNameUnique(deptDTO.getDeptName(), deptDTO.getParentId(), null)) {
            throw new BizException(ErrorCode.DEPT_NAME_EXISTS, "部门名称已存在");
        }

        SysDept dept = new SysDept();
        BeanUtils.copyProperties(deptDTO, dept);
        dept.setParentId(deptDTO.getParentId() != null ? deptDTO.getParentId() : 0L);
        dept.setOrderNum(deptDTO.getOrderNum() != null ? deptDTO.getOrderNum() : 0);
        dept.setStatus(StringUtils.hasText(deptDTO.getStatus()) ? deptDTO.getStatus() : "0");
        dept.setDelFlag("0");

        if (dept.getParentId() != null && dept.getParentId() != 0L) {
            SysDept parentDept = this.getById(dept.getParentId());
            if (parentDept != null) {
                dept.setAncestors(parentDept.getAncestors() + "," + dept.getParentId());
            } else {
                dept.setAncestors("0," + dept.getParentId());
            }
        } else {
            dept.setAncestors("0");
        }

        String currentUser = SecurityContextUtils.getUsername();
        dept.setCreateBy(currentUser);
        dept.setCreateTime(LocalDateTime.now());

        return this.save(dept);
    }

    /**
     * 更新部门信息，父部门变更时级联更新子部门祖级列表。
     * <p>{@code @CacheEvict} 清空部门树缓存；{@code @Transactional} 异常时回滚。</p>
     *
     * @param deptDTO 部门信息（含 deptId）
     * @return 是否更新成功
     * @throws BizException 部门不存在、父部门非法或名称重复时抛出
     */
    @Override
    @CacheEvict(cacheNames = CacheConstants.DEPT_TREE, key = "'all'")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDept(DeptDTO deptDTO) {
        SysDept dept = this.getById(deptDTO.getDeptId());
        AssertUtils.notNull(dept, ErrorCode.DEPT_NOT_FOUND);
        if ("2".equals(dept.getDelFlag())) {
            throw new BizException(ErrorCode.DEPT_NOT_FOUND, "部门不存在");
        }

        if (deptDTO.getParentId() != null && deptDTO.getParentId().equals(deptDTO.getDeptId())) {
            throw new BizException(ErrorCode.DEPT_PARENT_ERROR, "不能将父部门设置为自己的子部门");
        }

        if (!checkDeptNameUnique(deptDTO.getDeptName(), deptDTO.getParentId(), deptDTO.getDeptId())) {
            throw new BizException(ErrorCode.DEPT_NAME_EXISTS, "部门名称已存在");
        }

        if (deptDTO.getParentId() != null && !deptDTO.getParentId().equals(dept.getParentId())) {
            SysDept newParentDept = this.getById(deptDTO.getParentId());
            if (newParentDept != null) {
                deptDTO.setAncestors(newParentDept.getAncestors() + "," + deptDTO.getParentId());
            } else {
                deptDTO.setAncestors("0," + deptDTO.getParentId());
            }
            updateChildrenAncestors(deptDTO.getDeptId(), deptDTO.getAncestors());
        }

        BeanUtils.copyProperties(deptDTO, dept, "deptId");
        String currentUser = SecurityContextUtils.getUsername();
        dept.setUpdateBy(currentUser);
        dept.setUpdateTime(LocalDateTime.now());

        return this.updateById(dept);
    }

    /**
     * 批量逻辑删除部门。
     * <p>{@code @CacheEvict} 清空部门树缓存；{@code @Transactional} 异常时回滚。</p>
     *
     * @param deptIds 待删除的部门 ID 列表
     * @return 有有效删除时返回 {@code true}，入参为空返回 {@code false}
     * @throws BizException 存在子部门或关联用户时抛出
     */
    @Override
    @CacheEvict(cacheNames = CacheConstants.DEPT_TREE, key = "'all'")
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDeptByIds(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return false;
        }

        String currentUser = SecurityContextUtils.getUsername();

        for (Long deptId : deptIds) {
            if (hasChildDept(deptId)) {
                throw new BizException(ErrorCode.DEPT_HAS_CHILDREN, "部门ID " + deptId + " 存在子部门，不允许删除");
            }
            if (hasUser(deptId)) {
                throw new BizException(ErrorCode.DEPT_HAS_USERS, "部门ID " + deptId + " 存在用户，不允许删除");
            }

            SysDept dept = this.getById(deptId);
            if (dept != null && !"2".equals(dept.getDelFlag())) {
                dept.setDelFlag("2");
                dept.setUpdateBy(currentUser);
                dept.setUpdateTime(LocalDateTime.now());
                this.updateById(dept);
            }
        }
        return true;
    }

    /**
     * 校验同级部门名称是否唯一。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param deptName 部门名称
     * @param parentId 父部门 ID，{@code null} 视为根节点
     * @param deptId   排除的部门 ID（更新时传入），新增时传 {@code null}
     * @return {@code true} 表示名称可用
     */
    @Override
    @Transactional(readOnly = true)
    public boolean checkDeptNameUnique(String deptName, Long parentId, Long deptId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getDeptName, deptName)
                .eq(SysDept::getParentId, parentId != null ? parentId : 0L)
                .eq(SysDept::getDelFlag, "0");
        if (deptId != null) {
            wrapper.ne(SysDept::getDeptId, deptId);
        }
        return this.count(wrapper) == 0;
    }

    /**
     * 判断部门是否存在未删除的子部门。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param deptId 部门 ID
     * @return {@code true} 表示存在子部门
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasChildDept(Long deptId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, deptId)
                .eq(SysDept::getDelFlag, "0");
        return this.count(wrapper) > 0;
    }

    /**
     * 判断部门下是否存在未删除的用户。
     * <p>{@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param deptId 部门 ID
     * @return {@code true} 表示存在关联用户
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasUser(Long deptId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeptId, deptId)
                .eq(SysUser::getDelFlag, "0");
        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 递归构建部门树，并按 {@code orderNum} 排序。
     *
     * @param depts    扁平部门列表
     * @param parentId 当前层父部门 ID
     * @return 子树节点列表
     */
    private List<DeptVO> buildDeptTree(List<SysDept> depts, Long parentId) {
        List<DeptVO> deptTree = new ArrayList<>();

        for (SysDept dept : depts) {
            if (parentId.equals(dept.getParentId())) {
                DeptVO deptVO = new DeptVO();
                BeanUtils.copyProperties(dept, deptVO);

                List<DeptVO> children = buildDeptTree(depts, dept.getDeptId());
                deptVO.setChildren(children.isEmpty() ? null : children);

                deptTree.add(deptVO);
            }
        }

        deptTree.sort((a, b) -> {
            int orderA = a.getOrderNum() != null ? a.getOrderNum() : 0;
            int orderB = b.getOrderNum() != null ? b.getOrderNum() : 0;
            return Integer.compare(orderA, orderB);
        });

        return deptTree;
    }

    /**
     * 递归更新子部门的祖级列表（父部门变更后调用）。
     *
     * @param deptId    当前部门 ID
     * @param ancestors 新的祖级路径
     */
    private void updateChildrenAncestors(Long deptId, String ancestors) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, deptId)
                .eq(SysDept::getDelFlag, "0");
        List<SysDept> children = this.list(wrapper);

        for (SysDept child : children) {
            String newAncestors = ancestors + "," + deptId;
            child.setAncestors(newAncestors);
            this.updateById(child);
            updateChildrenAncestors(child.getDeptId(), newAncestors);
        }
    }
}
