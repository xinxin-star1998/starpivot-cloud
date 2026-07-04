package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.bo.SysNoticeVO;
import cn.org.starpivot.system.domain.dto.SysNoticeDTO;
import cn.org.starpivot.system.domain.dto.SysNoticeQueryDTO;
import cn.org.starpivot.system.service.ISysNoticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知公告控制器。
 * <p>
 * 提供系统通知公告的分页查询、详情查看及增删改 REST 接口。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link RestController} — REST 控制器</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /notice}</li>
 *   <li>{@link Tag} — OpenAPI 分组「通知公告」</li>
 * </ul>
 *
 * @see ISysNoticeService
 */
@Slf4j
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
@Tag(name = "通知公告", description = "系统通知公告的增删改查等接口")
public class SysNoticeController
{
    private final ISysNoticeService sysNoticeService;

    /**
     * 分页查询通知公告列表
     * 
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @PreAuthorize("hasAuthority('system:notice:query')")
    @PostMapping("/noticePageList")
    public Result<PageResponse<SysNoticeVO>> noticePageList(@RequestBody SysNoticeQueryDTO queryDTO)
    {
        PageResponse<SysNoticeVO> page = sysNoticeService.selectSysNoticePage(queryDTO);
        return Result.success(page);
    }

    /**
     * 获取通知公告详细信息
     * 
     * @param noticeId 通知公告主键
     * @return 通知公告信息
     */
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping(value = "/getNoticeInfo/{noticeId}")
    public Result<SysNoticeVO> getNoticeInfo(@PathVariable("noticeId") Integer noticeId)
    {
        SysNoticeVO sysNoticeVO = sysNoticeService.selectSysNoticeByNoticeId(noticeId);
        return Result.success(sysNoticeVO);
    }

    /**
     * 新增通知公告
     * 
     * @param sysNoticeDTO 通知公告信息
     * @return 操作结果
     */
    @Log(title = "新增通知公告", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('system:notice:add')")
    @PostMapping
    public Result<?> addNotice(@Valid @RequestBody SysNoticeDTO sysNoticeDTO)
    {
        boolean success = sysNoticeService.insertSysNotice(sysNoticeDTO);
        return success ? Result.success("新增通知公告成功") : Result.error("新增通知公告失败");
    }

    /**
     * 修改通知公告
     * 
     * @param sysNoticeDTO 通知公告信息
     * @return 操作结果
     */
    @Log(title = "修改通知公告", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('system:notice:edit')")
    @PutMapping
    public Result<?> edit(@Valid @RequestBody SysNoticeDTO sysNoticeDTO)
    {
        boolean success = sysNoticeService.updateSysNotice(sysNoticeDTO);
        return success ? Result.success("修改通知公告成功") : Result.error("修改通知公告失败");
    }

    /**
     * 删除通知公告
     * 
     * @param deleteRequest 需要删除的通知公告主键数组
     * @return 操作结果
     */
    @Log(title = "删除通知公告", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:notice:delete')")
    @DeleteMapping("/removeNotice")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest)
    {
        List<Long> noticeIds = validateIds(deleteRequest.getIds());

        boolean success = sysNoticeService.deleteSysNoticeByNoticeIds(noticeIds);
       return success ? Result.success("删除通知公告成功") : Result.error("删除通知公告失败");
   }

    /**
     * 验证ID列表非空
     */
    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
