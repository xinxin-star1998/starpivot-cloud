package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.file.FileRefClient;
import cn.org.starpivot.api.file.dto.FileRefBindRequest;
import cn.org.starpivot.api.file.dto.FileRefBizRequest;
import cn.org.starpivot.api.file.dto.FileRefSyncRequest;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

public class FileRefClientFallbackFactory implements FallbackFactory<FileRefClient> {

    private static final String ACTION = "文件引用";

    @Override
    public FileRefClient create(Throwable cause) {
        return new FileRefClient() {
            @Override
            public Result<Void> bind(FileRefBindRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> unbind(FileRefBindRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Long> countByFileId(Long fileId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> syncByObjects(FileRefSyncRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> unbindByBiz(FileRefBizRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
