package cn.org.starpivot.system.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.common.entity.DeleteRequest;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.domain.bo.LogininforReqBo;
import cn.org.starpivot.system.domain.bo.LogininforVO;
import cn.org.starpivot.system.domain.entity.SysLogininfor;
import cn.org.starpivot.system.service.SysLogininforService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/logininfor")
@RequiredArgsConstructor
@Tag(name = "登录日志管理")
public class SysLogininforController {

    private final SysLogininforService sysLogininforService;

    @Log(title = "登录日志")
    @PreAuthorize("hasAuthority('system:logininfor:query')")
    @PostMapping("/pageList")
    public Result<PageResponse<LogininforVO>> pageList(@RequestBody LogininforReqBo logininforReqBo) {
        return Result.success(sysLogininforService.pageList(logininforReqBo));
    }

    @Log(title = "登录日志")
    @PreAuthorize("hasAuthority('system:logininfor:query')")
    @GetMapping("/{infoId}")
    public Result<LogininforVO> getLogininforById(@PathVariable Long infoId) {
        SysLogininfor logininfor = sysLogininforService.getById(infoId);
        if (logininfor == null) {
            return Result.error("登录日志不存在");
        }
        LogininforVO vo = new LogininforVO();
        BeanUtils.copyProperties(logininfor, vo);
        return Result.success(vo);
    }

    @Log(title = "删除登录日志", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('system:logininfor:delete')")
    @DeleteMapping("/delete")
    public Result<?> remove(@RequestBody DeleteRequest deleteRequest) {
        boolean success = sysLogininforService.removeByIds(validateIds(deleteRequest.getIds()));
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @Log(title = "清空登录日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("hasAuthority('system:logininfor:delete')")
    @DeleteMapping("/clean")
    public Result<?> clean() {
        boolean success = sysLogininforService.remove(null);
        return success ? Result.success("清空成功") : Result.error("清空失败");
    }

    private List<Long> validateIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        return ids;
    }
}
