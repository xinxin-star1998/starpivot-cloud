package cn.org.starpivot.api.file;

import cn.org.starpivot.api.file.dto.FileRefBindRequest;
import cn.org.starpivot.api.file.dto.FileRefBizRequest;
import cn.org.starpivot.api.file.dto.FileRefSyncRequest;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 文件引用 Feign 客户端（微服务内部调用）。
 */
@FeignClient(
        name = "starpivot-file",
        contextId = "fileRefClient",
        path = "/api/${starpivot.api.version:v1}")
public interface FileRefClient {

    @PostMapping("/internal/file/ref/bind")
    Result<Void> bind(@RequestBody FileRefBindRequest request);

    @DeleteMapping("/internal/file/ref/unbind")
    Result<Void> unbind(@RequestBody FileRefBindRequest request);

    @GetMapping("/internal/file/ref/count/{fileId}")
    Result<Long> countByFileId(@PathVariable("fileId") Long fileId);

    @PutMapping("/internal/file/ref/sync-by-objects")
    Result<Void> syncByObjects(@RequestBody FileRefSyncRequest request);

    @DeleteMapping("/internal/file/ref/unbind-by-biz")
    Result<Void> unbindByBiz(@RequestBody FileRefBizRequest request);
}
