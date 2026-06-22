package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.bo.DeptVO;
import cn.org.starpivot.system.domain.dto.DeptDTO;
import cn.org.starpivot.system.domain.entity.SysDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 部门管理服务接口。
 * <p>
 * 提供组织架构部门的树形查询、增删改及唯一性/关联校验。
 * </p>
 */
public interface SysDeptService extends IService<SysDept> {

    /** 查询完整部门树。 */
    List<DeptVO> selectDeptTree();

    /** 根据部门 ID 查询部门视图。 */
    DeptVO selectDeptById(Long deptId);

    /** 新增部门。 */
    boolean insertDept(DeptDTO deptDTO);

    /** 修改部门信息。 */
    boolean updateDept(DeptDTO deptDTO);

    /** 批量删除部门。 */
    boolean deleteDeptByIds(List<Long> deptIds);

    /** 校验同级部门名称是否唯一。 */
    boolean checkDeptNameUnique(String deptName, Long parentId, Long deptId);

    /** 判断部门是否存在子部门。 */
    boolean hasChildDept(Long deptId);

    /** 判断部门下是否存在用户。 */
    boolean hasUser(Long deptId);
}
