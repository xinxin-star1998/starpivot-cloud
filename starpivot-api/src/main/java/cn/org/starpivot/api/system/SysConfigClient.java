package cn.org.starpivot.api.system;

import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "starpivot-system", contextId = "sysConfigClient")
public interface SysConfigClient {

    @GetMapping("/api/v1/internal/config/register-enabled")
    Result<Boolean> isRegisterEnabled();
}