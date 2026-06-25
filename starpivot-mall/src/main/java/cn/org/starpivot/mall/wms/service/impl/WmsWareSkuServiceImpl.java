package cn.org.starpivot.mall.wms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.wms.domain.dto.WmsWareSkuDTO;
import cn.org.starpivot.mall.wms.domain.dto.WmsWareSkuInboundDTO;
import cn.org.starpivot.mall.wms.domain.dto.WmsWareSkuQueryDTO;
import cn.org.starpivot.mall.wms.domain.vo.WmsWareSkuVO;
import cn.org.starpivot.mall.wms.entity.WmsWareInfo;
import cn.org.starpivot.mall.wms.entity.WmsWareSku;
import cn.org.starpivot.mall.wms.mapper.WmsWareInfoMapper;
import cn.org.starpivot.mall.wms.mapper.WmsWareSkuMapper;
import cn.org.starpivot.mall.wms.service.WmsWareSkuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 仓库 SKU 库存服务实现类。
 * <p>
 * 实现 {@link WmsWareSkuService}，处理仓库 SKU 库存相关业务。
 * </p>
 * <ul>
 *   <li>{@link Slf4j} — 日志记录</li>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see WmsWareSkuService
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class WmsWareSkuServiceImpl extends ServiceImpl<WmsWareSkuMapper, WmsWareSku> implements WmsWareSkuService
{
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final WmsWareInfoMapper wmsWareInfoMapper;

    private static int calcAvailable(Long stock, Long stockLocked) {
        int s = stock == null ? 0 : stock.intValue();
        int locked = stockLocked == null ? 0 : stockLocked.intValue();
        return Math.max(0, s - locked);
    }

    /**
     * 分页查询商品库存列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResponse<WmsWareSkuVO> selectWmsWareSkuPage(WmsWareSkuQueryDTO queryDTO)
    {
        PageResponse<WmsWareSkuVO> pageResponse = new PageResponse<>();
        Page<WmsWareSku> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<WmsWareSku> wmsWareSkuPage = baseMapper.selectPageList(page, queryDTO);

        // 转换为VO
        java.util.List<WmsWareSkuVO> voList = wmsWareSkuPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());
        enrichVoList(voList);

        pageResponse.setTotal(wmsWareSkuPage.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(wmsWareSkuPage.getCurrent());
        pageResponse.setPageSize(wmsWareSkuPage.getSize());
        pageResponse.setPageCount(wmsWareSkuPage.getPages());
        return pageResponse;
    }

    /**
     * 新增商品库存
     * 
     * @param wmsWareSkuDTO 商品库存信息
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insertWmsWareSku(WmsWareSkuDTO wmsWareSkuDTO)
    {
        WmsWareSku wmsWareSku = new WmsWareSku();
        BeanUtils.copyProperties(wmsWareSkuDTO, wmsWareSku);
        return this.save(wmsWareSku);
    }

    /**
     * 修改商品库存
     * 
     * @param wmsWareSkuDTO 商品库存信息
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateWmsWareSku(WmsWareSkuDTO wmsWareSkuDTO)
    {
        WmsWareSku wmsWareSku = this.getById(wmsWareSkuDTO.getId());
        if (wmsWareSku == null) {
            throw new BizException("商品库存不存在");
        }
        
        BeanUtils.copyProperties(wmsWareSkuDTO, wmsWareSku, "id");
        return this.updateById(wmsWareSku);
    }

    /**
     * 批量删除商品库存
     * 
     * @param ids 需要删除的商品库存主键数组
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteWmsWareSkuByIds(Long[] ids)
    {
        return this.removeByIds(java.util.Arrays.asList(ids));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        if (skuId == null || wareId == null || skuNum == null || skuNum <= 0) {
            throw new BizException("入库参数无效");
        }
        WmsWareSku existing = this.getOne(new LambdaQueryWrapper<WmsWareSku>()
                .eq(WmsWareSku::getSkuId, skuId)
                .eq(WmsWareSku::getWareId, wareId));
        if (existing == null) {
            WmsWareSku wareSku = new WmsWareSku();
            wareSku.setSkuId(skuId);
            wareSku.setWareId(wareId);
            wareSku.setStock(skuNum);
            wareSku.setStockLocked(0);
            PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectById(skuId);
            if (skuInfo != null) {
                wareSku.setSkuName(skuInfo.getSkuName());
            }
            this.save(wareSku);
        } else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    /**
     * 根据主键查询商品库存详细信息
     *
     * @param id 商品库存主键
     * @return 商品库存信息
     */
    @Override
    public WmsWareSkuVO selectWmsWareSkuById(Long id)
    {
        WmsWareSku wmsWareSku = this.getById(id);
        if (wmsWareSku == null) {
            throw new BizException("商品库存不存在");
        }
        return enrichVo(convertToVO(wmsWareSku));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void inboundStock(WmsWareSkuInboundDTO dto) {
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectById(dto.getSkuId());
        if (skuInfo == null) {
            throw new BizException("SKU 不存在");
        }
        WmsWareInfo wareInfo = wmsWareInfoMapper.selectById(dto.getWareId());
        if (wareInfo == null) {
            throw new BizException("仓库不存在");
        }
        addStock(dto.getSkuId(), dto.getWareId(), dto.getSkuNum());
    }

    private void enrichVoList(List<WmsWareSkuVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        Set<Long> wareIds = voList.stream()
                .map(WmsWareSkuVO::getWareId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> skuIds = voList.stream()
                .map(WmsWareSkuVO::getSkuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> wareNameMap = wareIds.isEmpty()
                ? Collections.emptyMap()
                : wmsWareInfoMapper.selectBatchIds(wareIds).stream()
                        .filter(w -> w.getId() != null)
                        .collect(Collectors.toMap(WmsWareInfo::getId, WmsWareInfo::getName, (a, b) -> a));

        Map<Long, Integer> warningMap = skuIds.isEmpty()
                ? Collections.emptyMap()
                : pmsSkuInfoMapper.selectBatchIds(skuIds).stream()
                        .filter(s -> s.getSkuId() != null)
                        .collect(Collectors.toMap(PmsSkuInfo::getSkuId, s -> s.getStockWarning() == null ? 0 : s.getStockWarning(), (a, b) -> a));

        for (WmsWareSkuVO vo : voList) {
            if (vo.getWareId() != null) {
                vo.setWareName(wareNameMap.get(vo.getWareId()));
            }
            if (vo.getSkuId() != null) {
                vo.setStockWarning(warningMap.get(vo.getSkuId()));
            }
            vo.setAvailableStock(calcAvailable(vo.getStock(), vo.getStockLocked()));
        }
    }

    private WmsWareSkuVO enrichVo(WmsWareSkuVO vo) {
        enrichVoList(List.of(vo));
        return vo;
    }

    /**
     * 转换为VO
     * 
     * @param wmsWareSku 实体对象
     * @return VO对象
     */
    private WmsWareSkuVO convertToVO(WmsWareSku wmsWareSku)
    {
        WmsWareSkuVO vo = new WmsWareSkuVO();
        BeanUtils.copyProperties(wmsWareSku, vo);
        if (wmsWareSku.getStock() != null) {
            vo.setStock(wmsWareSku.getStock().longValue());
        }
        if (wmsWareSku.getStockLocked() != null) {
            vo.setStockLocked(wmsWareSku.getStockLocked().longValue());
        }
        vo.setAvailableStock(calcAvailable(vo.getStock(), vo.getStockLocked()));
        return vo;
    }
}
