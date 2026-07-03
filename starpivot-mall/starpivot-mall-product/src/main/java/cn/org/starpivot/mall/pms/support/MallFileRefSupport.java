package cn.org.starpivot.mall.pms.support;

import cn.org.starpivot.api.file.FileRefClient;
import cn.org.starpivot.api.file.dto.FileRefBizRequest;
import cn.org.starpivot.api.file.dto.FileRefSyncRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 商城素材与文件中心引用同步（Feign 调用，失败时不阻断主业务）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MallFileRefSupport {

    private final FileRefClient fileRefClient;

    public void syncBrandLogo(Long brandId, String logo) {
        if (brandId == null) {
            return;
        }
        List<String> objectNames = new ArrayList<>(1);
        String normalized = normalizeObjectName(logo);
        if (StringUtils.hasText(normalized)) {
            objectNames.add(normalized);
        }
        syncObjects(MallFileRefConstants.BIZ_TYPE_BRAND, String.valueOf(brandId), objectNames);
    }

    public void unbindBrand(Long brandId) {
        if (brandId == null) {
            return;
        }
        unbindByBiz(MallFileRefConstants.BIZ_TYPE_BRAND, String.valueOf(brandId));
    }

    public void syncSpuImages(Long spuId, Collection<String> imageUrls) {
        if (spuId == null) {
            return;
        }
        syncObjects(MallFileRefConstants.BIZ_TYPE_SPU, String.valueOf(spuId), normalizeList(imageUrls));
    }

    public void syncSkuImages(Long skuId, Collection<String> imageUrls) {
        if (skuId == null) {
            return;
        }
        syncObjects(MallFileRefConstants.BIZ_TYPE_SKU, String.valueOf(skuId), normalizeList(imageUrls));
    }

    public void unbindProduct(Long spuId, List<Long> skuIds) {
        if (spuId != null) {
            unbindByBiz(MallFileRefConstants.BIZ_TYPE_SPU, String.valueOf(spuId));
        }
        if (skuIds == null) {
            return;
        }
        for (Long skuId : skuIds) {
            if (skuId != null) {
                unbindByBiz(MallFileRefConstants.BIZ_TYPE_SKU, String.valueOf(skuId));
            }
        }
    }

    private void syncObjects(String bizType, String bizId, List<String> objectNames) {
        FileRefSyncRequest request = new FileRefSyncRequest();
        request.setBizType(bizType);
        request.setBizId(bizId);
        request.setObjectNames(objectNames);
        try {
            Result<Void> result = fileRefClient.syncByObjects(request);
            if (result != null && !result.isSuccess()) {
                log.warn("同步文件引用失败: bizType={}, bizId={}, message={}", bizType, bizId, result.getMessage());
            }
        } catch (Exception ex) {
            log.warn("同步文件引用失败（不影响业务保存）: bizType={}, bizId={}, error={}",
                    bizType, bizId, ex.getMessage());
        }
    }

    private void unbindByBiz(String bizType, String bizId) {
        FileRefBizRequest request = new FileRefBizRequest();
        request.setBizType(bizType);
        request.setBizId(bizId);
        try {
            Result<Void> result = fileRefClient.unbindByBiz(request);
            if (result != null && !result.isSuccess()) {
                log.warn("解除文件引用失败: bizType={}, bizId={}, message={}", bizType, bizId, result.getMessage());
            }
        } catch (Exception ex) {
            log.warn("解除文件引用失败（不影响业务保存）: bizType={}, bizId={}, error={}",
                    bizType, bizId, ex.getMessage());
        }
    }

    private List<String> normalizeList(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        Set<String> distinct = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = normalizeObjectName(value);
            if (StringUtils.hasText(normalized)) {
                distinct.add(normalized);
            }
        }
        return new ArrayList<>(distinct);
    }

    private String normalizeObjectName(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return StorageObjectPathUtils.normalizeToObjectName(value.trim());
    }
}
