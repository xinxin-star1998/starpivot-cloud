package cn.org.starpivot.mall.mall.controller.internal;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.service.PortalAvailableStockService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;

@Hidden
@RestController
@RequestMapping("/internal/mall/stock")
@RequiredArgsConstructor
public class MallStockInternalController {

    private final PortalAvailableStockService portalAvailableStockService;

    @PostMapping("/available")
    public Result<Map<Long, Integer>> getAvailableStockMap(@RequestBody Collection<Long> skuIds) {
        return Result.success(portalAvailableStockService.getAvailableStockMap(skuIds));
    }
}
