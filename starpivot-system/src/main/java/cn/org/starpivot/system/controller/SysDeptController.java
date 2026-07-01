package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.domain.bo.DeptVO;
import cn.org.starpivot.system.domain.dto.DeptDTO;
import cn.org.starpivot.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器。
 * <p>
 * 提供组织架构部门的树形查询、增删改等 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /sys/dept}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入 {@link SysDeptService}</li>
 *   <li>{@link Tag} — OpenAPI 分组「部门管理」</li>
 * </ul>
 */
@RestController
@RequestMapping("/sys/dept")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门的增删改查、部门树查询等接口")
public class SysDeptController {

    private final SysDeptService deptService;

    /**
     * 查询完整部门树。
     *
     * @return 树形结构的部门视图列表
     */
    @Operation(summary = "查询部门树")
    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/tree")
    public Result<List<DeptVO>> tree() {
        return Result.success(deptService.selectDeptTree());
    }

    /**
     * 根据部门 ID 获取详情。
     *
     * @param deptId 部门主键
     * @return 部门视图对象
     */
    @Operation(summary = "获取部门详情")
    @PreAuthorize("hasAuthority('system:dept:query')")
    @GetMapping("/{deptId}")
    public Result<DeptVO> getInfo(@Parameter(description = "部门ID") @PathVariable("deptId") Long deptId) {
        return Result.success(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门。
     *
     * @param deptDTO 部门创建参数
     * @return 操作结果
     */
    @Log(title = "新增部门", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:dept:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody DeptDTO deptDTO) {
        boolean success = deptService.insertDept(deptDTO);
        return success ? Result.success("新增部门成功") : Result.error("新增部门失败");
    }

    /**
     * 修改部门信息。
     *
     * @param deptDTO 部门更新参数
     * @return 操作结果
     */
    @Log(title = "修改部门", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody DeptDTO deptDTO) {
        boolean success = deptService.updateDept(deptDTO);
        return success ? Result.success("修改部门成功") : Result.error("修改部门失败");
    }

    /**
     * 批量删除部门。
     *
     * @param deleteRequest 待删除部门 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除部门", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:dept:delete')")
    @DeleteMapping("/removeDept")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        boolean success = deptService.deleteDeptByIds(validateIds(deleteRequest.getIds()));
        return success ? Result.success("删除部门成功") : Result.error("删除部门失败");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
