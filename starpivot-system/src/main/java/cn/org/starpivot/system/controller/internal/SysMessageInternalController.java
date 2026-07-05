package cn.org.starpivot.system.controller.internal;

import cn.org.starpivot.api.system.dto.MessageSendRequest;
import cn.org.starpivot.api.system.dto.MessageSendToRolesRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.service.ISysUserMessageService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/internal/message")
@RequiredArgsConstructor
public class SysMessageInternalController {

    private final ISysUserMessageService messageService;

    @PostMapping("/send")
    public Result<Void> send(@Valid @RequestBody MessageSendRequest request) {
        messageService.sendMessage(request);
        return Result.success();
    }

    @PostMapping("/send-to-roles")
    public Result<Void> sendToRoles(@Valid @RequestBody MessageSendToRolesRequest request) {
        messageService.sendToRoles(request);
        return Result.success();
    }
}
