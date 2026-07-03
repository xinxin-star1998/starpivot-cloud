package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.api.approval.dto.InternalApprovalSubmitRequest;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.MallApprovalConstants;
import cn.org.starpivot.mall.common.MallApprovalSubmitter;
import cn.org.starpivot.mall.common.MallAuditStatus;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.pms.search.PmsProductSearchSyncService;
import cn.org.starpivot.mall.pms.service.PmsSpuApprovalService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PmsSpuApprovalServiceImpl implements PmsSpuApprovalService {

    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final MallApprovalSubmitter mallApprovalSubmitter;
    private final PmsProductSearchSyncService productSearchSyncService;

    @Override
    public void submitApproval(Long spuId) {
        PmsSpuInfo spu = validateForSubmit(spuId);
        Long userId = mallApprovalSubmitter.requireUserId();
        mallApprovalSubmitter.submit(
                () -> markPending(spuId),
                () -> buildSubmitRequest(spu, userId),
                instanceId -> bindInstanceId(spuId, instanceId),
                () -> rollbackDraft(spuId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleApprovalFinished(String bizModule, String bizType, String bizKey, String result, String comment) {
        if (!MallApprovalConstants.BIZ_MODULE.equals(bizModule)
                || !MallApprovalConstants.BIZ_TYPE_SPU.equals(bizType)) {
            return;
        }
        Long spuId = MallApprovalConstants.parseSpuId(bizKey);
        if (spuId == null) {
            log.warn("无法解析 SPU bizKey: {}", bizKey);
            return;
        }
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(spuId);
        if (spu == null) {
            log.warn("审批完结但 SPU 不存在: {}", spuId);
            return;
        }
        if (!MallAuditStatus.PENDING.equals(spu.getAuditStatus())) {
            log.info("跳过非待审状态的 SPU 完结事件: id={}, auditStatus={}, result={}",
                    spuId, spu.getAuditStatus(), result);
            return;
        }

        PmsSpuInfo patch = new PmsSpuInfo();
        patch.setId(spuId);
        patch.setUpdateTime(LocalDateTime.now());
        switch (result) {
            case MallAuditStatus.APPROVED -> {
                patch.setAuditStatus(MallAuditStatus.APPROVED);
                patch.setPublishStatus(1);
            }
            case MallAuditStatus.REJECTED -> patch.setAuditStatus(MallAuditStatus.REJECTED);
            case MallAuditStatus.WITHDRAWN -> patch.setAuditStatus(MallAuditStatus.WITHDRAWN);
            default -> {
                log.warn("未知审批结果: {}", result);
                return;
            }
        }
        pmsSpuInfoMapper.updateById(patch);
        if (MallAuditStatus.APPROVED.equals(result)) {
            productSearchSyncService.syncPublishedSpu(spuId);
        }
        log.info("SPU 上架审批完结: id={}, result={}, comment={}", spuId, result, comment);
    }

    private PmsSpuInfo validateForSubmit(Long spuId) {
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(spuId);
        if (spu == null) {
            throw new BizException("商品不存在");
        }
        if (Objects.equals(spu.getPublishStatus(), 1)) {
            throw new BizException("商品已上架，无需重复提交审批");
        }
        if (!MallAuditStatus.canSubmit(spu.getAuditStatus())) {
            throw new BizException("当前审批状态不可重复提交");
        }
        List<PmsSkuInfo> skus = pmsSkuInfoMapper.selectList(
                Wrappers.<PmsSkuInfo>lambdaQuery().eq(PmsSkuInfo::getSpuId, spuId));
        if (CollectionUtils.isEmpty(skus)) {
            throw new BizException("商品未配置 SKU，无法提交上架审批");
        }
        return spu;
    }

    private void markPending(Long spuId) {
        PmsSpuInfo patch = new PmsSpuInfo();
        patch.setId(spuId);
        patch.setAuditStatus(MallAuditStatus.PENDING);
        patch.setUpdateTime(LocalDateTime.now());
        pmsSpuInfoMapper.updateById(patch);
    }

    private InternalApprovalSubmitRequest buildSubmitRequest(PmsSpuInfo spu, Long userId) {
        InternalApprovalSubmitRequest request = new InternalApprovalSubmitRequest();
        request.setBizModule(MallApprovalConstants.BIZ_MODULE);
        request.setBizType(MallApprovalConstants.BIZ_TYPE_SPU);
        request.setBizKey(MallApprovalConstants.spuBizKey(spu.getId()));
        request.setTitle("商品上架审批 #" + spu.getId() + " · " + spu.getSpuName());
        request.setStarterId(userId);
        Map<String, Object> context = new HashMap<>();
        context.put("catalogId", spu.getCatalogId());
        context.put("brandId", spu.getBrandId());
        request.setContext(context);
        return request;
    }

    private void bindInstanceId(Long spuId, Long instanceId) {
        PmsSpuInfo patch = new PmsSpuInfo();
        patch.setId(spuId);
        patch.setApprovalInstanceId(instanceId);
        patch.setAuditStatus(MallAuditStatus.PENDING);
        patch.setUpdateTime(LocalDateTime.now());
        pmsSpuInfoMapper.updateById(patch);
    }

    private void rollbackDraft(Long spuId) {
        PmsSpuInfo rollback = new PmsSpuInfo();
        rollback.setId(spuId);
        rollback.setAuditStatus(MallAuditStatus.DRAFT);
        rollback.setUpdateTime(LocalDateTime.now());
        pmsSpuInfoMapper.updateById(rollback);
    }
}
