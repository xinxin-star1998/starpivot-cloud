package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.bo.OperLogReqBo;
import cn.org.starpivot.system.domain.bo.OperLogVO;
import cn.org.starpivot.system.domain.entity.SysOperLog;
import cn.org.starpivot.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志管理控制器。
 * <p>
 * 提供系统操作审计日志的分页查询、详情查看、删除及清空接口。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /sys/operlog}</li>
 *   <li>{@link Tag} — OpenAPI 分组「操作日志管理」</li>
 * </ul>
 */
@RestController
@RequestMapping("/sys/operlog")
@RequiredArgsConstructor
@Tag(name = "操作日志管理")
public class SysOperLogController {

    private final SysOperLogService sysOperLogService;

    /**
     * 分页查询操作日志。
     *
     * @param operLogReqBo 分页及筛选条件
     * @return 操作日志视图分页结果
     */
    @Log(title = "操作日志")
    @PreAuthorize("hasAuthority('system:operlog:query')")
    @PostMapping("/pageList")
    public Result<PageResponse<OperLogVO>> pageList(@RequestBody OperLogReqBo operLogReqBo) {
        return Result.success(sysOperLogService.pageList(operLogReqBo));
    }

    /**
     * 根据日志 ID 获取操作日志详情。
     *
     * @param operId 操作日志主键
     * @return 操作日志视图对象
     */
    @Log(title = "操作日志")
    @PreAuthorize("hasAuthority('system:operlog:query')")
    @GetMapping("/{operId}")
    public Result<OperLogVO> getOperLogById(@PathVariable Long operId) {
        SysOperLog operLog = sysOperLogService.getById(operId);
        if (operLog == null) {
            return Result.error("操作日志不存在");
        }
        OperLogVO vo = new OperLogVO();
        BeanUtils.copyProperties(operLog, vo);
        return Result.success(vo);
    }

    /**
     * 批量删除操作日志。
     *
     * @param deleteRequest 待删除日志 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除操作日志", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:operlog:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        boolean success = sysOperLogService.removeByIds(validateIds(deleteRequest.getIds()));
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 清空全部操作日志。
     *
     * @return 操作结果
     */
    @Log(title = "清空操作日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("hasAuthority('system:operlog:delete')")
    @DeleteMapping("/clean")
    public Result<?> clean() {
        boolean success = sysOperLogService.remove(null);
        return success ? Result.success("清空成功") : Result.error("清空失败");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
