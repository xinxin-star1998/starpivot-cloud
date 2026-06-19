package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.system.domain.bo.SysDictTypeVO;
import cn.org.starpivot.system.domain.dto.SysDictTypeDTO;
import cn.org.starpivot.system.domain.dto.SysDictTypeQueryDTO;
import cn.org.starpivot.system.domain.entity.SysDictData;
import cn.org.starpivot.system.domain.entity.SysDictType;
import cn.org.starpivot.system.mapper.SysDictDataMapper;
import cn.org.starpivot.system.mapper.SysDictTypeMapper;
import cn.org.starpivot.system.service.SysDictTypeService;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.util.AssertUtils;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典类型服务实现类
 *
 * @author stardust
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictDataMapper sysDictDataMapper;

    @Override
    public PageResponse<SysDictTypeVO> selectDictTypePage(SysDictTypeQueryDTO queryDTO) {
        PageResponse<SysDictTypeVO> pageResponse = new PageResponse<>();
        Page<SysDictType> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        wrapper.like(StringUtils.hasText(queryDTO.getDictName()), SysDictType::getDictName, queryDTO.getDictName())
                .like(StringUtils.hasText(queryDTO.getDictType()), SysDictType::getDictType, queryDTO.getDictType())
                .eq(StringUtils.hasText(queryDTO.getStatus()), SysDictType::getStatus, queryDTO.getStatus())
                .orderByDesc(SysDictType::getCreateTime);

        IPage<SysDictType> dictTypePage = this.page(page, wrapper);

        // 转换为VO
        IPage<SysDictTypeVO> voPage = new Page<>(dictTypePage.getCurrent(), dictTypePage.getSize(), dictTypePage.getTotal());
        List<SysDictTypeVO> voList = dictTypePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        pageResponse.setTotal(dictTypePage.getTotal());
        pageResponse.setRows(voPage.getRecords());
        pageResponse.setPageNum(voPage.getCurrent());
        pageResponse.setPageSize(voPage.getSize());
        return pageResponse;
    }

    @Override
    public SysDictTypeVO selectDictTypeById(Long dictId) {
        SysDictType dictType = this.getById(dictId);
        AssertUtils.notNull(dictType, ErrorCode.DICT_TYPE_NOT_FOUND);
        return convertToVO(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertDictType(SysDictTypeDTO dictTypeDTO) {
        if (!checkDictTypeUnique(dictTypeDTO.getDictType(), null)) {
            throw new BizException(ErrorCode.DICT_TYPE_EXISTS, "字典类型已存在");
        }

        // 创建字典类型
        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(dictTypeDTO, dictType);
        dictType.setStatus(StringUtils.hasText(dictTypeDTO.getStatus()) ? dictTypeDTO.getStatus() : "0");

        String currentUser = SecurityUtils.getUsername();
        dictType.setCreateBy(currentUser);
        dictType.setCreateTime(LocalDateTime.now());

        return this.save(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictType(SysDictTypeDTO dictTypeDTO) {
        SysDictType dictType = this.getById(dictTypeDTO.getDictId());
        AssertUtils.notNull(dictType, ErrorCode.DICT_TYPE_NOT_FOUND);

        if (!checkDictTypeUnique(dictTypeDTO.getDictType(), dictTypeDTO.getDictId())) {
            throw new BizException(ErrorCode.DICT_TYPE_EXISTS, "字典类型已被使用");
        }

        if (!dictType.getDictType().equals(dictTypeDTO.getDictType())) {
            LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDictData::getDictType, dictType.getDictType());
            List<SysDictData> dictDataList = sysDictDataMapper.selectList(wrapper);
            for (SysDictData dictData : dictDataList) {
                dictData.setDictType(dictTypeDTO.getDictType());
                sysDictDataMapper.updateById(dictData);
            }
        }

        // 更新字典类型信息
        BeanUtils.copyProperties(dictTypeDTO, dictType, "dictId");
        String currentUser = SecurityUtils.getUsername();
        dictType.setUpdateBy(currentUser);
        dictType.setUpdateTime(LocalDateTime.now());

        return this.updateById(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictTypeByIds(List<Long> dictIds) {
        if (dictIds == null || dictIds.isEmpty()) {
            return false;
        }

        for (Long dictId : dictIds) {
            SysDictType dictType = this.getById(dictId);
            if (dictType != null) {
                // 检查是否有字典数据
                LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysDictData::getDictType, dictType.getDictType());
                long count = sysDictDataMapper.selectCount(wrapper);
                if (count > 0) {
                    throw new BizException("字典类型[" + dictType.getDictName() + "]存在字典数据，不能删除");
                }

                this.removeById(dictId);
            }
        }
        return true;
    }

    @Override
    public boolean checkDictTypeUnique(String dictType, Long dictId) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictType::getDictType, dictType);
        if (dictId != null) {
            wrapper.ne(SysDictType::getDictId, dictId);
        }
        return this.count(wrapper) == 0;
    }

    @Override
    public List<SysDictTypeVO> selectList() {
        return this.list().stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private SysDictTypeVO convertToVO(SysDictType dictType) {
        SysDictTypeVO vo = new SysDictTypeVO();
        BeanUtils.copyProperties(dictType, vo);
        return vo;
    }
}