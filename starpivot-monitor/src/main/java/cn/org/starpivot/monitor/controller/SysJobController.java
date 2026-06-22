package cn.org.starpivot.monitor.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
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

/**
 * 定时任务及执行日志管理 REST 接口。
 * <p>
 * {@link RestController}：返回 JSON 响应；
 * {@link RequestMapping}：统一前缀 {@code /monitor/job}；
 * {@link RequiredArgsConstructor}：注入 {@link ISysJobService}。
 */
@RestController
@RequestMapping("/monitor/job")
@RequiredArgsConstructor
public class SysJobController {

    private final ISysJobService sysJobService;

    /**
     * 分页查询定时任务列表。
     *
     * @param query 查询条件与分页参数
     * @return 任务分页结果
     */
    @PreAuthorize("hasAuthority('monitor:job:query')")
    @PostMapping("/list")
    public Result<PageResponse<SysJobVO>> list(@RequestBody SysJobQueryDTO query) {
        return Result.success(sysJobService.selectJobPage(query));
    }

    /**
     * 根据 ID 查询定时任务详情。
     *
     * @param jobId 任务 ID
     * @return 任务详情
     */
    @PreAuthorize("hasAuthority('monitor:job:query')")
    @GetMapping("/{jobId}")
    public Result<SysJobVO> getInfo(@PathVariable Long jobId) {
        return Result.success(sysJobService.getJobById(jobId));
    }

    /**
     * 新增定时任务。
     *
     * @param dto 任务配置
     * @return 操作结果
     */
    @Log(title = "新增定时任务", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('monitor:job:add')")
    @PostMapping
    public Result<?> add(@Valid @RequestBody SysJobDTO dto) {
        sysJobService.insertJob(dto);
        return Result.success("新增成功");
    }

    /**
     * 修改定时任务。
     *
     * @param dto 任务配置（须含任务 ID）
     * @return 操作结果
     */
    @Log(title = "修改定时任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody SysJobDTO dto) {
        sysJobService.updateJob(dto);
        return Result.success("修改成功");
    }

    /**
     * 批量删除定时任务。
     *
     * @param request 待删除的任务 ID 列表
     * @return 操作结果
     */
    @Log(title = "删除定时任务", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('monitor:job:delete')")
    @DeleteMapping
    public Result<?> remove(@RequestBody DeleteRequest request) {
        sysJobService.deleteJobByIds(validateIds(request.getIds()));
        return Result.success("删除成功");
    }

    /**
     * 启用或暂停定时任务。
     *
     * @param commonDto 任务 ID 与目标状态
     * @return 操作结果
     */
    @Log(title = "修改定时任务状态", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @PostMapping("/changeStatus")
    public Result<?> changeStatus(@RequestBody SysJobCommonDto commonDto) {
        sysJobService.changeStatus(commonDto.getJobId(), commonDto.getStatus());
        return Result.success("操作成功");
    }

    /**
     * 立即触发一次任务执行。
     *
     * @param jobId 任务 ID
     * @return 操作结果
     */
    @PreAuthorize("hasAuthority('monitor:job:edit')")
    @PutMapping("/run/{jobId}")
    public Result<?> runOnce(@PathVariable Long jobId) {
        sysJobService.runOnce(jobId);
        return Result.success("已触发执行");
    }

    /**
     * 分页查询任务执行日志。
     *
     * @param query 查询条件与分页参数
     * @return 日志分页结果
     */
    @PreAuthorize("hasAuthority('monitor:job:query')")
    @PostMapping("/log/list")
    public Result<PageResponse<SysJobLogVO>> logList(@RequestBody SysJobLogQueryDTO query) {
        return Result.success(sysJobService.selectJobLogPage(query));
    }

    /**
     * 清空全部任务执行日志。
     *
     * @return 操作结果
     */
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
