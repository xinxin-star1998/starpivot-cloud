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

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private final SysUserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConstants.DEPT_TREE, key = "'all'")
    public List<DeptVO> selectDeptTree() {
        List<SysDept> depts = this.list(new LambdaQueryWrapper<SysDept>().eq(SysDept::getDelFlag, "0"));
        return buildDeptTree(depts, 0L);
    }

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

    @Override
    @CacheEvict(cacheNames = CacheConstants.DEPT_TREE, allEntries = true)
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

    @Override
    @CacheEvict(cacheNames = CacheConstants.DEPT_TREE, allEntries = true)
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

    @Override
    @CacheEvict(cacheNames = CacheConstants.DEPT_TREE, allEntries = true)
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

    @Override
    @Transactional(readOnly = true)
    public boolean hasChildDept(Long deptId) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, deptId)
                .eq(SysDept::getDelFlag, "0");
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUser(Long deptId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeptId, deptId)
                .eq(SysUser::getDelFlag, "0");
        return userMapper.selectCount(wrapper) > 0;
    }

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
