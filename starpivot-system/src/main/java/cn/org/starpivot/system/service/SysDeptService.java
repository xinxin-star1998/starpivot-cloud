package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.bo.DeptVO;
import cn.org.starpivot.system.domain.dto.DeptDTO;
import cn.org.starpivot.system.domain.entity.SysDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    List<DeptVO> selectDeptTree();

    DeptVO selectDeptById(Long deptId);

    boolean insertDept(DeptDTO deptDTO);

    boolean updateDept(DeptDTO deptDTO);

    boolean deleteDeptByIds(List<Long> deptIds);

    boolean checkDeptNameUnique(String deptName, Long parentId, Long deptId);

    boolean hasChildDept(Long deptId);

    boolean hasUser(Long deptId);
}
