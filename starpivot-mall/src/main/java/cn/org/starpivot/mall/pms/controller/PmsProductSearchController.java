package cn.org.starpivot.mall.pms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.pms.search.PmsProductSearchSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/mall/product-search")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-商品搜索索引", description = "Elasticsearch 商品索引维护")
public class PmsProductSearchController {

    private final PmsProductSearchSyncService productSearchSyncService;

    @Operation(summary = "全量重建上架商品 ES 索引")
    @PostMapping("/reindex")
    @PreAuthorize("hasAuthority('mall:product:edit')")
    public Result<Map<String, Object>> reindex() {
        if (!productSearchSyncService.isElasticsearchActive()) {
            return Result.error("Elasticsearch 未启用，请在配置中开启 starpivot.mall.elasticsearch.enabled");
        }
        int count = productSearchSyncService.reindexAllPublished();
        return Result.success(Map.of("count", count));
    }
}
