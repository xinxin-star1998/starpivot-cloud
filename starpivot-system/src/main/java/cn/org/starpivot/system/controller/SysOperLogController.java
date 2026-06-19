package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.domain.Result;
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

@RestController
@RequestMapping("/sys/operlog")
@RequiredArgsConstructor
@Tag(name = "操作日志管理")
public class SysOperLogController {

    private final SysOperLogService sysOperLogService;

    @Log(title = "操作日志")
    @PreAuthorize("hasAuthority('system:operlog:query')")
    @PostMapping("/pageList")
    public Result<PageResponse<OperLogVO>> pageList(@RequestBody OperLogReqBo operLogReqBo) {
        return Result.success(sysOperLogService.pageList(operLogReqBo));
    }

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

    @Log(title = "删除操作日志", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:operlog:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        boolean success = sysOperLogService.removeByIds(validateIds(deleteRequest.getIds()));
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

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
