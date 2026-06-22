package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.security.SecurityContextUtils;
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
 * 字典数据服务实现类。
 * <p>实现 {@link SysDictDataService}，含字典数据 CRUD 及按类型缓存查询。</p>
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final SysDictDataMapper sysDictDataMapper;

    /**
     * 分页查询字典数据列表，支持按标签、类型、状态筛选。
     * <p>使用 {@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param queryDTO 查询条件与分页参数
     * @return {@link SysDictDataVO} 分页结果
     */
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

    /**
     * 根据字典类型查询字典数据，结果按排序号升序。
     * <p>使用 {@code @Transactional(readOnly = true)} 只读事务；
     * 使用 {@code @Cacheable} 以 {@code dictType} 为键缓存查询结果。</p>
     *
     * @param dictType 字典类型
     * @return {@link SysDictDataVO} 列表
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConstants.SYS_DICT, key = "#dictType")
    public List<SysDictDataVO> selectDictDataByType(String dictType) {
        List<SysDictData> dictDataList = sysDictDataMapper.selectDictDataByType(dictType);
        return dictDataList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据主键查询字典数据详情。
     * <p>使用 {@code @Transactional(readOnly = true)} 只读事务。</p>
     *
     * @param dictCode 字典数据主键
     * @return 字典数据视图对象
     * @throws cn.org.starpivot.common.exception.BizException 字典数据不存在时抛出
     */
    @Override
    @Transactional(readOnly = true)
    public SysDictDataVO selectDictDataById(Long dictCode) {
        SysDictData dictData = this.getById(dictCode);
        AssertUtils.notNull(dictData, ErrorCode.DICT_NOT_FOUND);
        return convertToVO(dictData);
    }

    /**
     * 新增字典数据。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)} 写事务；
     * 使用 {@code @CacheEvict(allEntries = true)} 清除全部字典缓存。</p>
     *
     * @param dictDataDTO 字典数据信息
     * @return 是否新增成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConstants.SYS_DICT, allEntries = true)
    public boolean insertDictData(SysDictDataDTO dictDataDTO) {
        // 创建字典数据
        SysDictData dictData = new SysDictData();
        BeanUtils.copyProperties(dictDataDTO, dictData);
        dictData.setDictSort(dictDataDTO.getDictSort() != null ? dictDataDTO.getDictSort() : 0);
        dictData.setIsDefault(StringUtils.hasText(dictDataDTO.getIsDefault()) ? dictDataDTO.getIsDefault() : "N");
        dictData.setStatus(StringUtils.hasText(dictDataDTO.getStatus()) ? dictDataDTO.getStatus() : "0");

        String currentUser = SecurityContextUtils.getUsername();
        dictData.setCreateBy(currentUser);
        dictData.setCreateTime(LocalDateTime.now());

        return this.save(dictData);
    }

    /**
     * 修改字典数据。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)} 写事务；
     * 使用 {@code @CacheEvict(allEntries = true)} 清除全部字典缓存。</p>
     *
     * @param dictDataDTO 字典数据信息
     * @return 是否修改成功
     * @throws cn.org.starpivot.common.exception.BizException 字典数据不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConstants.SYS_DICT, allEntries = true)
    public boolean updateDictData(SysDictDataDTO dictDataDTO) {
        SysDictData dictData = this.getById(dictDataDTO.getDictCode());
        AssertUtils.notNull(dictData, ErrorCode.DICT_NOT_FOUND);

        BeanUtils.copyProperties(dictDataDTO, dictData, "dictCode");
        String currentUser = SecurityContextUtils.getUsername();
        dictData.setUpdateBy(currentUser);
        dictData.setUpdateTime(LocalDateTime.now());

        return this.updateById(dictData);
    }

    /**
     * 批量删除字典数据。
     * <p>使用 {@code @Transactional(rollbackFor = Exception.class)} 写事务；
     * 使用 {@code @CacheEvict(allEntries = true)} 清除全部字典缓存。</p>
     *
     * @param dictCodes 待删除的字典数据主键列表
     * @return 删除成功返回 {@code true}；列表为空返回 {@code false}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheConstants.SYS_DICT, allEntries = true)
    public boolean deleteDictDataByIds(List<Long> dictCodes) {
        if (dictCodes == null || dictCodes.isEmpty()) {
            return false;
        }
        return this.removeByIds(dictCodes);
    }

    /**
     * 将 {@link SysDictData} 实体转换为 {@link SysDictDataVO}。
     *
     * @param dictData 字典数据实体
     * @return 字典数据视图对象
     */
    private SysDictDataVO convertToVO(SysDictData dictData) {
        SysDictDataVO vo = new SysDictDataVO();
        BeanUtils.copyProperties(dictData, vo);
        return vo;
    }
}