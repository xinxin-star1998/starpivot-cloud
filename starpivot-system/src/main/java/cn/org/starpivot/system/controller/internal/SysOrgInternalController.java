package cn.org.starpivot.system.controller.internal;

import cn.org.starpivot.api.system.dto.AssigneeResolveRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.system.service.OrgAssigneeService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@RestController
@RequestMapping("/internal/org")
@RequiredArgsConstructor
public class SysOrgInternalController {

    private final OrgAssigneeService orgAssigneeService;

    @PostMapping("/assignees/resolve")
    public Result<List<Long>> resolveAssignees(@RequestBody AssigneeResolveRequest request) {
        return Result.success(orgAssigneeService.resolveAssignees(request));
    }

    @GetMapping("/user/{userId}/display-name")
    public Result<String> displayName(@PathVariable Long userId) {
        return Result.success(orgAssigneeService.displayName(userId));
    }
}
