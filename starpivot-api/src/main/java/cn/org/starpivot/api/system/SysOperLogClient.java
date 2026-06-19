package cn.org.starpivot.api.system;

import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;

@FeignClient(name = "starpivot-system", contextId = "sysOperLogClient")
public interface SysOperLogClient {

    @DeleteMapping("/internal/operlog/clean")
    Result<Void> cleanAll();
}
