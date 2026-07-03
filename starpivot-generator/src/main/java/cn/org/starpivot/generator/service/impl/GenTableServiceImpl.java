package cn.org.starpivot.generator.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.generator.domain.bo.GenTableVO;
import cn.org.starpivot.generator.domain.dto.GenTableQueryDTO;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.mapper.GenTableColumnMapper;
import cn.org.starpivot.generator.mapper.GenTableMapper;
import cn.org.starpivot.generator.service.GenTableService;
import cn.org.starpivot.generator.service.support.GenTableCodegenSupport;
import cn.org.starpivot.generator.service.support.GenTableImportSupport;
import cn.org.starpivot.generator.service.support.GenTableMetadataSupport;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 代码生成表业务服务实现，提供表导入、同步、预览与 ZIP 下载等能力。
 */
@Service
@RequiredArgsConstructor
public class GenTableServiceImpl extends ServiceImpl<GenTableMapper, GenTable> implements GenTableService {

    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper genTableColumnMapper;
    private final GenTableMetadataSupport metadataSupport;
    private final GenTableImportSupport importSupport;
    private final GenTableCodegenSupport codegenSupport;

    @Override
    public PageResponse<GenTableVO> selectGenTablePage(GenTableQueryDTO queryDTO) {
        Page<GenTable> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<GenTable> genTablePage = genTableMapper.selectGenTableList(page, queryDTO);
        return toPageResponse(genTablePage, queryDTO);
    }

    @Override
    public PageResponse<GenTableVO> selectDbTableList(GenTableQueryDTO queryDTO) {
        Page<GenTable> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<GenTable> genTablePage = genTableMapper.selectDbTableList(page, queryDTO);
        return toPageResponse(genTablePage, queryDTO);
    }

    @Override
    @Transactional
    public void deleteGenTableByIds(List<Long> tableIds) {
        genTableMapper.deleteGenTableByIds(tableIds);
        genTableColumnMapper.deleteGenTableColumnByIds(tableIds);
    }

    @Override
    public Map<String, String> previewCode(Long tableId) {
        return codegenSupport.previewCode(tableId);
    }

    @Override
    public byte[] downloadCode(String tableName) {
        return codegenSupport.downloadCode(tableName);
    }

    @Override
    @Transactional
    public void synchDb(String tableName) {
        importSupport.synchDb(tableName);
    }

    @Override
    public byte[] downloadCode(String[] tableNames) {
        return codegenSupport.downloadCode(tableNames);
    }

    @Override
    public List<GenTable> selectGenTableAll() {
        return genTableMapper.selectGenTableAll();
    }

    @Override
    public GenTable selectGenTableById(Long id) {
        GenTable genTable = genTableMapper.selectGenTableById(id);
        metadataSupport.applyOptionsFromJson(genTable);
        return genTable;
    }

    @Override
    public boolean createTable(String sql) {
        return genTableMapper.createTable(sql) == 0;
    }

    @Override
    public List<GenTable> selectDbTableListByNames(String[] tableNames) {
        return genTableMapper.selectDbTableListByNames(tableNames);
    }

    @Override
    @Transactional
    public void importGenTable(List<GenTable> tableList, String operName) {
        importSupport.importGenTable(tableList, operName);
    }

    @Override
    @Transactional
    public void updateGenTable(GenTable genTable) {
        String options = JSON.toJSONString(genTable.getParams());
        genTable.setOptions(options);
        int row = genTableMapper.updateGenTable(genTable);
        if (row > 0) {
            for (GenTableColumn genTableColumn : genTable.getColumns()) {
                genTableColumnMapper.updateGenTableColumn(genTableColumn);
            }
        }
    }

    @Override
    public void validateEdit(GenTable genTable) {
        metadataSupport.validateEdit(genTable);
    }

    private PageResponse<GenTableVO> toPageResponse(IPage<GenTable> genTablePage, GenTableQueryDTO queryDTO) {
        List<GenTableVO> voList = genTablePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResponse<GenTableVO> pageResponse = new PageResponse<>();
        pageResponse.setTotal(genTablePage.getTotal());
        pageResponse.setRows(voList);
        pageResponse.setPageNum(Long.valueOf(queryDTO.getPageNum()));
        pageResponse.setPageSize(Long.valueOf(queryDTO.getPageSize()));
        pageResponse.setPageCount(genTablePage.getPages());
        return pageResponse;
    }

    private GenTableVO convertToVO(GenTable genTable) {
        GenTableVO vo = new GenTableVO();
        BeanUtils.copyProperties(genTable, vo);
        return vo;
    }
}
