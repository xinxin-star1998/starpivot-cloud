package cn.org.starpivot.monitor.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.job.domain.bo.SysJobLogVO;
import cn.org.starpivot.job.domain.bo.SysJobVO;
import cn.org.starpivot.job.domain.dto.SysJobCommonDto;
import cn.org.starpivot.job.domain.dto.SysJobDTO;
import cn.org.starpivot.job.domain.dto.SysJobLogQueryDTO;
import cn.org.starpivot.job.domain.dto.SysJobQueryDTO;
import cn.org.starpivot.job.service.ISysJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final ISysJobService sysJobService;

    @PreAuthorize("hasAuthority('monitor:job:query')")
    @PostMapping("/list")
    public Result<PageResponse<SysJobVO>> list(@RequestBody SysJobQueryDTO query) {
        return Result.success(sysJobService.selectJobPage(query));
    }

    @PreAuthorize("hasAuthority('monitor:job:query')")
    @GetMapping("/{jobId}")
    public Result<SysJobVO> getInfo(@PathVariable Long jobId) {
        return Result.success(sysJobService.getJobById(jobId));
    }

    @Log(title = "新增定时任务", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('monitor:job:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody SysJobDTO dto) {
        sysJobService.insertJob(dto);
        return Result.success("新增成功");
    }

    @Log(title = "修改定时任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody SysJobDTO dto) {
        sysJobService.updateJob(dto);
        return Result.success("修改成功");
    }

    @Log(title = "删除定时任务", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('monitor:job:delete')")
    @DeleteMapping
    public Result<?> remove(@RequestBody DeleteRequest request) {
        sysJobService.deleteJobByIds(validateIds(request.getIds()));
        return Result.success("删除成功");
    }

    @Log(title = "修改定时任务状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @PostMapping("/changeStatus")
    public Result<?> changeStatus(@RequestBody SysJobCommonDto commonDto) {
        sysJobService.changeStatus(commonDto.getJobId(), commonDto.getStatus());
        return Result.success("操作成功");
    }

    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @PutMapping("/run/{jobId}")
    public Result<?> runOnce(@PathVariable Long jobId) {
        sysJobService.runOnce(jobId);
        return Result.success("已触发执行");
    }

    @PreAuthorize("hasAuthority('monitor:job:query')")
    @PostMapping("/log/list")
    public Result<PageResponse<SysJobLogVO>> logList(@RequestBody SysJobLogQueryDTO query) {
        return Result.success(sysJobService.selectJobLogPage(query));
    }

    @Log(title = "清空定时任务日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("hasAuthority('monitor:job:delete')")
    @DeleteMapping("/log/clear")
    public Result<?> clearLog() {
        sysJobService.clearJobLog();
        return Result.success("清空成功");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
