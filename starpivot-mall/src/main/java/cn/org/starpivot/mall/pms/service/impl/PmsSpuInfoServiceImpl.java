package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.common.MallAuditStatus;
import cn.org.starpivot.mall.pms.domain.bo.ProductReqBo;
import cn.org.starpivot.mall.pms.domain.bo.ProductSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.ProductVo;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.pms.search.PmsProductSearchSyncService;
import cn.org.starpivot.mall.pms.service.PmsSpuInfoService;
import cn.org.starpivot.mall.pms.support.PmsSpuRelationSupport;
import cn.org.starpivot.mall.pms.support.PmsSpuVoAssembler;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SPU 服务实现：主表 CRUD 编排，关联表与 VO 组装委托 support 组件。
 *
 * @see PmsSpuInfoService
 */
@Service
@RequiredArgsConstructor
public class PmsSpuInfoServiceImpl implements PmsSpuInfoService {

    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsProductSearchSyncService productSearchSyncService;
    private final PmsSpuVoAssembler pmsSpuVoAssembler;
    private final PmsSpuRelationSupport pmsSpuRelationSupport;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductVo> getPmsSpuInfoPageList(ProductReqBo productReqBo) {
        Page<PmsSpuInfo> page = new Page<>(productReqBo.getPageNum(), productReqBo.getPageSize());
        IPage<PmsSpuInfo> pageList = pmsSpuInfoMapper.selectPageList(page, productReqBo);
        List<ProductVo> rows = pageList.getRecords().stream()
                .map(pmsSpuVoAssembler::toVo)
                .collect(Collectors.toList());
        pmsSpuVoAssembler.fillCoverImages(rows);
        rows.forEach(pmsSpuVoAssembler::normalizeVoImagePaths);
        PageResponse<ProductVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(rows);
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVo getPmsSpuInfoById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SPU ID不能为空");
        }
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(id);
        if (spu == null) {
            throw new BizException("商品不存在");
        }
        ProductVo vo = pmsSpuVoAssembler.toVo(spu);
        pmsSpuVoAssembler.fillSpuRelations(vo, id);
        pmsSpuVoAssembler.normalizeVoImagePaths(vo);
        pmsSpuVoAssembler.fillCoverImages(Collections.singletonList(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPmsSpuInfo(ProductSaveBo bo) {
        PmsSpuInfo entity = new PmsSpuInfo();
        copySpuMainFields(bo, entity);
        entity.setId(null);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        if (entity.getPublishStatus() == null) {
            entity.setPublishStatus(0);
        }
        entity.setAuditStatus(MallAuditStatus.DRAFT);
        pmsSpuInfoMapper.insert(entity);
        pmsSpuRelationSupport.saveSpuRelations(entity.getId(), bo, true);
        pmsSpuRelationSupport.syncProductFileRefs(entity.getId());
        productSearchSyncService.syncPublishedSpu(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePmsSpuInfo(ProductSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改时 id 不能为空");
        }
        PmsSpuInfo existing = pmsSpuInfoMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("商品不存在");
        }
        copySpuMainFields(bo, existing);
        existing.setUpdateTime(LocalDateTime.now());
        pmsSpuInfoMapper.updateById(existing);
        pmsSpuRelationSupport.unbindProductFileRefs(bo.getId());
        pmsSpuRelationSupport.removeSpuRelations(bo.getId());
        pmsSpuRelationSupport.saveSpuRelations(bo.getId(), bo, false);
        pmsSpuRelationSupport.syncProductFileRefs(bo.getId());
        productSearchSyncService.syncPublishedSpu(bo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePmsSpuInfoByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        List<Long> spuIds =
                ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        for (Long spuId : spuIds) {
            pmsSpuRelationSupport.unbindProductFileRefs(spuId);
            pmsSpuRelationSupport.removeSpuRelations(spuId);
            productSearchSyncService.removeSpu(spuId);
        }
        pmsSpuInfoMapper.delete(Wrappers.<PmsSpuInfo>lambdaQuery().in(PmsSpuInfo::getId, spuIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePublishStatus(Long id, Integer publishStatus) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SPU ID不能为空");
        }
        if (publishStatus == null || (publishStatus != 0 && publishStatus != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "上架状态仅支持 0（下架）或 1（上架）");
        }
        PmsSpuInfo existing = pmsSpuInfoMapper.selectById(id);
        if (existing == null) {
            throw new BizException("商品不存在");
        }
        if (publishStatus == 1) {
            throw new BizException("上架需先提交审批，请使用「提交审批」");
        }
        existing.setPublishStatus(publishStatus);
        existing.setUpdateTime(LocalDateTime.now());
        pmsSpuInfoMapper.updateById(existing);
        if (publishStatus == 0) {
            productSearchSyncService.removeSpu(id);
        } else {
            productSearchSyncService.syncPublishedSpu(id);
        }
    }

    private void copySpuMainFields(ProductSaveBo bo, PmsSpuInfo entity) {
        entity.setSpuName(bo.getSpuName());
        entity.setSpuDescription(bo.getSpuDescription());
        entity.setCatalogId(bo.getCatalogId());
        entity.setBrandId(bo.getBrandId());
        entity.setWeight(bo.getWeight());
        entity.setPublishStatus(bo.getPublishStatus());
    }
}
