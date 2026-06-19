package cn.org.starpivot.api.system;

import cn.org.starpivot.api.system.dto.LoginLogDto;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "starpivot-system", contextId = "sysLoginLogClient")
public interface SysLoginLogClient {

    @PostMapping("/internal/logininfor")
    Result<Void> saveLoginLog(@RequestBody LoginLogDto loginLogDto);
}
