package cn.org.starpivot.file.controller.internal;

import cn.org.starpivot.api.file.dto.FileRefBindRequest;
import cn.org.starpivot.api.file.dto.FileRefBizRequest;
import cn.org.starpivot.api.file.dto.FileRefSyncRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.file.service.ISysFileRefService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文件引用内部接口（Feign 调用，不经网关）。
 */
@RestController
@RequestMapping("/internal/file/ref")
@RequiredArgsConstructor
public class SysFileRefInternalController {

    private final ISysFileRefService sysFileRefService;

    @PostMapping("/bind")
    public Result<Void> bind(@Valid @RequestBody FileRefBindRequest request) {
        sysFileRefService.bind(request.getFileId(), request.getBizType(), request.getBizId());
        return Result.success();
    }

    @DeleteMapping("/unbind")
    public Result<Void> unbind(@Valid @RequestBody FileRefBindRequest request) {
        sysFileRefService.unbind(request.getFileId(), request.getBizType(), request.getBizId());
        return Result.success();
    }

    @GetMapping("/count/{fileId}")
    public Result<Long> countByFileId(@PathVariable Long fileId) {
        return Result.success(sysFileRefService.countByFileId(fileId));
    }

    @PutMapping("/sync-by-objects")
    public Result<Void> syncByObjects(@Valid @RequestBody FileRefSyncRequest request) {
        sysFileRefService.syncByObjectNames(
                request.getBizType(), request.getBizId(), request.getObjectNames());
        return Result.success();
    }

    @DeleteMapping("/unbind-by-biz")
    public Result<Void> unbindByBiz(@Valid @RequestBody FileRefBizRequest request) {
        sysFileRefService.unbindAllByBiz(request.getBizType(), request.getBizId());
        return Result.success();
    }
}
