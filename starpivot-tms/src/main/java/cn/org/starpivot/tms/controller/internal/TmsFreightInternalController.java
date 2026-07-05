package cn.org.starpivot.tms.controller.internal;

import cn.org.starpivot.api.tms.dto.FreightCalculateRequest;
import cn.org.starpivot.api.tms.dto.FreightCalculateResult;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.tms.service.TmsFreightRuleService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequiredArgsConstructor
public class TmsFreightInternalController {

    private final TmsFreightRuleService freightRuleService;

    @PostMapping("/internal/tms/freight/calculate")
    public Result<FreightCalculateResult> calculate(@RequestBody FreightCalculateRequest request) {
        return Result.success(freightRuleService.calculate(request));
    }
}
