package cn.org.starpivot.generator.service.impl;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.config.GenConfig;
import cn.org.starpivot.generator.domain.bo.GenTableVO;
import cn.org.starpivot.generator.domain.dto.GenTableQueryDTO;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.external.GenTableCodegenHelper;
import cn.org.starpivot.generator.mapper.GenTableColumnMapper;
import cn.org.starpivot.generator.mapper.GenTableMapper;
import cn.org.starpivot.generator.service.GenTableService;
import cn.org.starpivot.generator.utils.GenUtils;
import cn.org.starpivot.generator.utils.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成表服务实现类
 *
 * @author xinxin
 * @since 2025-01-17
 */
@Slf4j
@Service
public class GenTableServiceImpl extends ServiceImpl<GenTableMapper, GenTable> implements GenTableService {
    @Autowired
    private GenTableMapper genTableMapper;
    @Autowired
    private GenTableColumnMapper genTableColumnMapper;
    @Autowired
    private GenConfig genConfig;
    @Override
    public PageResponse<GenTableVO> selectGenTablePage(GenTableQueryDTO queryDTO) {
        Page<GenTable> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 通过 XML 中自定义 SQL 查询
        IPage<GenTable> genTablePage = genTableMapper.selectGenTableList(page, queryDTO);
        
        // 转换为VO
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
    /**
     * 查询据库列表
     * @param queryDTO 业务信息
     * @return 数据库表集合
     */
    @Override
    public PageResponse<GenTableVO> selectDbTableList(GenTableQueryDTO queryDTO) {
        Page<GenTable> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 通过 XML 中自定义 SQL 查询
        IPage<GenTable> genTablePage = genTableMapper.selectDbTableList(page, queryDTO);
        // 转换为VO
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

    /**
     * 删除业务对象
     *
     * @param tableIds 需要删除的数据ID
     */
    @Override
    @Transactional
    public void deleteGenTableByIds(List<Long> tableIds)
    {
        genTableMapper.deleteGenTableByIds(tableIds);
        genTableColumnMapper.deleteGenTableColumnByIds(tableIds);
    }
    /**
     * 预览代码
     *
     * @param tableId 表编号
     * @return 预览数据列表
     */
    @Override
    public Map<String, String> previewCode(Long tableId)
    {
        GenTable table = genTableMapper.selectGenTableById(tableId);
        setSubTable(table);
        setPkColumn(table);
        return GenTableCodegenHelper.renderPrepared(table);
    }
    /**
     * 生成代码（下载方式）
     *
     * @param tableName 表名称
     * @return 数据
     */
    @Override
    public byte[] downloadCode(String tableName)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            generatorCode(tableName, zip);
        } catch (IOException e) {
            log.error("生成代码压缩包失败，表名: {}", tableName, e);
            throw new BizException("生成代码失败：" + e.getMessage());
        }
        return outputStream.toByteArray();
    }

    /**
     * 同步数据库
     *
     * @param tableName 表名称
     */
    @Override
    @Transactional
    public void synchDb(String tableName)
    {
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        if (StringUtils.isNull(table))
        {
            throw new BizException("同步数据失败，代码生成表不存在");
        }
        List<GenTableColumn> tableColumns = table.getColumns();
        if (StringUtils.isEmpty(tableColumns))
        {
            throw new BizException("同步数据失败，代码生成表字段列表为空");
        }
        Map<String, GenTableColumn> tableColumnMap = tableColumns.stream().collect(Collectors.toMap(GenTableColumn::getColumnName, Function.identity()));

        List<GenTableColumn> dbTableColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
        if (StringUtils.isEmpty(dbTableColumns))
        {
            throw new BizException("同步数据失败，原表结构不存在");
        }
        List<String> dbTableColumnNames = dbTableColumns.stream().map(GenTableColumn::getColumnName).toList();

        dbTableColumns.forEach(column -> {
            GenUtils.initColumnField(column, table);
            if (tableColumnMap.containsKey(column.getColumnName()))
            {
                GenTableColumn prevColumn = tableColumnMap.get(column.getColumnName());
                column.setColumnId(prevColumn.getColumnId());
                if (column.isList())
                {
                    // 如果是列表，继续保留查询方式/字典类型选项
                    column.setDictType(prevColumn.getDictType());
                    column.setQueryType(prevColumn.getQueryType());
                }
                if (StringUtils.isNotEmpty(prevColumn.getIsRequired()) && !column.isPk()
                        && (column.isInsert() || column.isEdit())
                        && ((column.isUsableColumn()) || (!column.isSuperColumn())))
                {
                    // 如果是(新增/修改&非主键/非忽略及父属性)，继续保留必填/显示类型选项
                    column.setIsRequired(prevColumn.getIsRequired());
                    column.setHtmlType(prevColumn.getHtmlType());
                }
                genTableColumnMapper.updateGenTableColumn(column);
            }
            else
            {
                genTableColumnMapper.insertGenTableColumn(column);
            }
        });

        List<GenTableColumn> delColumns = tableColumns.stream().filter(column -> !dbTableColumnNames.contains(column.getColumnName())).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(delColumns))
        {
            genTableColumnMapper.deleteGenTableColumns(delColumns);
        }
    }
    /**
     * 批量生成代码（下载方式）
     *
     * @param tableNames 表数组
     * @return 数据
     */
    @Override
    public byte[] downloadCode(String[] tableNames)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            for (String tableName : tableNames) {
                generatorCode(tableName, zip);
            }
        } catch (IOException e) {
            log.error("批量生成代码压缩包失败", e);
            throw new BizException("批量生成代码失败");
        }
        return outputStream.toByteArray();
    }

    /**
     * 查询表信息并生成代码
     */
    private void generatorCode(String tableName, ZipOutputStream zip) throws IOException
    {
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        if (StringUtils.isNull(table))
        {
            throw new BizException("生成代码失败，代码生成表不存在");
        }
        setSubTable(table);
        setPkColumn(table);
        GenTableCodegenHelper.writeZipPrepared(table, zip);
    }
    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    @Override
    public List<GenTable> selectGenTableAll()
    {
        return genTableMapper.selectGenTableAll();
    }
    /**
     * 查询业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    @Override
    public GenTable selectGenTableById(Long id)
    {
        GenTable genTable = genTableMapper.selectGenTableById(id);
        setTableFromOptions(genTable);
        return genTable;
    }
    /**
     * 创建表
     *
     * @param sql 创建表语句
     * @return 结果
     */
    @Override
    public boolean createTable(String sql) {
        return genTableMapper.createTable(sql) == 0;
    }
    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @return 数据库表集合
     */
    @Override
    public List<GenTable> selectDbTableListByNames(String[] tableNames)
    {
        return genTableMapper.selectDbTableListByNames(tableNames);
    }
    /**
     * 导入表结构
     *
     * @param tableList 导入表列表
     */
    @Override
    @Transactional
    public void importGenTable(List<GenTable> tableList, String operName)
    {
        try
        {
            for (GenTable table : tableList)
            {
                String tableName = table.getTableName();
                GenUtils.initTable(table, operName, genConfig);
                int row = genTableMapper.insertGenTable(table);
                if (row > 0)
                {
                    // 保存列信息
                    List<GenTableColumn> genTableColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
                    for (GenTableColumn column : genTableColumns)
                    {
                        GenUtils.initColumnField(column, table);
                        genTableColumnMapper.insertGenTableColumn(column);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new BizException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 修改业务
     *
     * @param genTable 业务信息
     */
    @Override
    @Transactional
    public void updateGenTable(GenTable genTable)
    {
        String options = JSON.toJSONString(genTable.getParams());
        genTable.setOptions(options);
        int row = genTableMapper.updateGenTable(genTable);
        if (row > 0)
        {
            for (GenTableColumn genTableColumn : genTable.getColumns())
            {
                genTableColumnMapper.updateGenTableColumn(genTableColumn);
            }
        }
    }

    /**
     * 修改保存参数校验
     *
     * @param genTable 业务信息
     */
    @Override
    public void validateEdit(GenTable genTable)
    {
        if (GenConstants.TPL_TREE.equals(genTable.getTplCategory()))
        {
            String options = JSON.toJSONString(genTable.getParams());
            JSONObject paramsObj = JSON.parseObject(options);
            if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_CODE)))
            {
                throw new BizException("树编码字段不能为空");
            }
            else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_PARENT_CODE)))
            {
                throw new BizException("树父编码字段不能为空");
            }
            else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_NAME)))
            {
                throw new BizException("树名称字段不能为空");
            }
        }
        else if (GenConstants.TPL_SUB.equals(genTable.getTplCategory()))
        {
            if (StringUtils.isEmpty(genTable.getSubTableName()))
            {
                throw new BizException("关联子表的表名不能为空");
            }
            else if (StringUtils.isEmpty(genTable.getSubTableFkName()))
            {
                throw new BizException("子表关联的外键名不能为空");
            }
        }
    }
    /**
     * 转换为VO
     */
    private GenTableVO convertToVO(GenTable genTable) {
        GenTableVO vo = new GenTableVO();
        BeanUtils.copyProperties(genTable, vo);
        return vo;
    }
    /**
     * 设置主键列信息
     *
     * @param table 业务表信息
     */
    public void setPkColumn(GenTable table)
    {
        GenTableCodegenHelper.setPkColumn(table);
    }

    /**
     * 设置主子表信息
     *
     * @param table 业务表信息
     */
    public void setSubTable(GenTable table)
    {
        String subTableName = table.getSubTableName();
        if (StringUtils.isNotEmpty(subTableName))
        {
            table.setSubTable(genTableMapper.selectGenTableByName(subTableName));
        }
    }
    /**
     * 设置代码生成其他选项值
     *
     * @param genTable 设置后的生成对象
     */
    public void setTableFromOptions(GenTable genTable)
    {
        String options = genTable.getOptions();
        if (StringUtils.isEmpty(options))
        {
            return;
        }
        JSONObject paramsObj = JSON.parseObject(options);
        if (StringUtils.isNotNull(paramsObj))
        {
            String treeCode = paramsObj.getString(GenConstants.TREE_CODE);
            String treeParentCode = paramsObj.getString(GenConstants.TREE_PARENT_CODE);
            String treeName = paramsObj.getString(GenConstants.TREE_NAME);
            Long parentMenuId = paramsObj.getLongValue(GenConstants.PARENT_MENU_ID);
            String parentMenuName = paramsObj.getString(GenConstants.PARENT_MENU_NAME);

            genTable.setTreeCode(treeCode);
            genTable.setTreeParentCode(treeParentCode);
            genTable.setTreeName(treeName);
            genTable.setParentMenuId(parentMenuId);
            genTable.setParentMenuName(parentMenuName);
        }
    }
}

