package cn.org.starpivot.mall.wms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.wms.domain.bo.WmsWareInfoReqBo;
import cn.org.starpivot.mall.wms.domain.bo.WmsWareInfoSaveBo;
import cn.org.starpivot.mall.wms.domain.vo.WmsWareInfoVo;
import cn.org.starpivot.mall.wms.entity.WmsWareInfo;
import cn.org.starpivot.mall.wms.mapper.WmsWareInfoMapper;
import cn.org.starpivot.mall.wms.service.WmsWareInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓库服务实现类。
 * <p>
 * 实现 {@link WmsWareInfoService}，处理仓库相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 * </ul>
 *
 * @see WmsWareInfoService
 */

@Service
public class WmsWareInfoServiceImpl extends ServiceImpl<WmsWareInfoMapper, WmsWareInfo>
        implements WmsWareInfoService {

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WmsWareInfoVo> getWmsWareInfoPageList(WmsWareInfoReqBo wmsWareInfoReqBo) {
        PageResponse<WmsWareInfoVo> pageResponse = new PageResponse<>();
        Page<WmsWareInfo> page = new Page<>(wmsWareInfoReqBo.getPageNum(), wmsWareInfoReqBo.getPageSize());
        IPage<WmsWareInfo> wmsWareInfoPage = baseMapper.selectPageList(page, wmsWareInfoReqBo);

        List<WmsWareInfoVo> voList =
                wmsWareInfoPage.getRecords().stream().map(this::convertToVo).collect(Collectors.toList());

        pageResponse.setTotal(wmsWareInfoPage.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(wmsWareInfoPage.getCurrent());
        pageResponse.setPageSize(wmsWareInfoPage.getSize());
        pageResponse.setPageCount(wmsWareInfoPage.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public WmsWareInfoVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "仓库ID不能为空");
        }
        WmsWareInfo entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new BizException("仓库不存在");
        }
        return convertToVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWare(WmsWareInfoSaveBo bo) {
        WmsWareInfo entity = new WmsWareInfo();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        baseMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWare(WmsWareInfoSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "仓库ID不能为空");
        }
        WmsWareInfo existing = baseMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("仓库不存在");
        }
        WmsWareInfo entity = new WmsWareInfo();
        BeanUtils.copyProperties(bo, entity);
        baseMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        super.removeByIds(ids);
    }

    private WmsWareInfoVo convertToVo(WmsWareInfo wmsWareInfo) {
        WmsWareInfoVo vo = new WmsWareInfoVo();
        BeanUtils.copyProperties(wmsWareInfo, vo);
        return vo;
    }
}
