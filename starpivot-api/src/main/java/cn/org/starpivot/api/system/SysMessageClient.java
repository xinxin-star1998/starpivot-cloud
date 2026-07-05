package cn.org.starpivot.api.system;

import cn.org.starpivot.api.fallback.SysMessageClientFallbackFactory;
import cn.org.starpivot.api.system.dto.MessageSendRequest;
import cn.org.starpivot.api.system.dto.MessageSendToRolesRequest;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 站内消息 Feign 客户端（各微服务通过内部接口投递消息）。
 */
@FeignClient(
        name = "starpivot-system",
        contextId = "sysMessageClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = SysMessageClientFallbackFactory.class)
public interface SysMessageClient {

    @PostMapping("/internal/message/send")
    Result<Void> send(@RequestBody MessageSendRequest request);

    @PostMapping("/internal/message/send-to-roles")
    Result<Void> sendToRoles(@RequestBody MessageSendToRolesRequest request);
}
