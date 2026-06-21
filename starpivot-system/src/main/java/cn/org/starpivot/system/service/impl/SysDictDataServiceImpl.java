package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.utils.SecurityUtils;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.system.domain.bo.SysDictDataVO;
import cn.org.starpivot.system.domain.dto.SysDictDataDTO;
import cn.org.starpivot.system.domain.dto.SysDictDataQueryDTO;
import cn.org.starpivot.system.domain.entity.SysDictData;
import cn.org.starpivot.system.mapper.SysDictDataMapper;
import cn.org.starpivot.system.service.SysDictDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典数据服务实现类
 *
 * @author stardust
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final SysDictDataMapper sysDictDataMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SysDictDataVO> selectDictDataPage(SysDictDataQueryDTO queryDTO) {
        Page<SysDictData> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        wrapper.like(StringUtils.hasText(queryDTO.getDictLabel()), SysDictData::getDictLabel, queryDTO.getDictLabel())
                .eq(StringUtils.hasText(queryDTO.getDictType()), SysDictData::getDictType, queryDTO.getDictType())
                .eq(StringUtils.hasText(queryDTO.getStatus()), SysDictData::getStatus, queryDTO.getStatus())
                .orderByAsc(SysDictData::getDictSort);

        IPage<SysDictData> dictDataPage = this.page(page, wrapper);

        // 转换为VO
        List<SysDictDataVO> voList = dictDataPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        PageResponse<SysDictDataVO> pageResponse = new PageResponse<>();
        pageResponse.setTotal(dictDataPage.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(Long.valueOf(queryDTO.getPageNum()));
        pageResponse.setPageSize(Long.valueOf(queryDTO.getPageSize()));
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "dictData", key = "#dictType")
    public List<SysDictDataVO> selectDictDataByType(String dictType) {
        List<SysDictData> dictDataList = sysDictDataMapper.selectDictDataByType(dictType);
        return dictDataList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SysDictDataVO selectDictDataById(Long dictCode) {
        SysDictData dictData = this.getById(dictCode);
        AssertUtils.notNull(dictData, ErrorCode.DICT_NOT_FOUND);
        return convertToVO(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "dictData", allEntries = true)
    public boolean insertDictData(SysDictDataDTO dictDataDTO) {
        // 创建字典数据
        SysDictData dictData = new SysDictData();
        BeanUtils.copyProperties(dictDataDTO, dictData);
        dictData.setDictSort(dictDataDTO.getDictSort() != null ? dictDataDTO.getDictSort() : 0);
        dictData.setIsDefault(StringUtils.hasText(dictDataDTO.getIsDefault()) ? dictDataDTO.getIsDefault() : "N");
        dictData.setStatus(StringUtils.hasText(dictDataDTO.getStatus()) ? dictDataDTO.getStatus() : "0");

        String currentUser = SecurityUtils.getUsername();
        dictData.setCreateBy(currentUser);
        dictData.setCreateTime(LocalDateTime.now());

        return this.save(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "dictData", allEntries = true)
    public boolean updateDictData(SysDictDataDTO dictDataDTO) {
        SysDictData dictData = this.getById(dictDataDTO.getDictCode());
        AssertUtils.notNull(dictData, ErrorCode.DICT_NOT_FOUND);

        BeanUtils.copyProperties(dictDataDTO, dictData, "dictCode");
        String currentUser = SecurityUtils.getUsername();
        dictData.setUpdateBy(currentUser);
        dictData.setUpdateTime(LocalDateTime.now());

        return this.updateById(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "dictData", allEntries = true)
    public boolean deleteDictDataByIds(List<Long> dictCodes) {
        if (dictCodes == null || dictCodes.isEmpty()) {
            return false;
        }
        return this.removeByIds(dictCodes);
    }

    /**
     * 转换为VO
     */
    private SysDictDataVO convertToVO(SysDictData dictData) {
        SysDictDataVO vo = new SysDictDataVO();
        BeanUtils.copyProperties(dictData, vo);
        return vo;
    }
}