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
 * 代码生成表业务服务实现，提供表导入、同步、预览与 ZIP 下载等能力。
 * <p>
 * 实现 {@link GenTableService}，依赖 {@link GenTableMapper} 与 {@link GenTableColumnMapper} 持久化数据，
 * 并通过 {@link GenTableCodegenHelper} 渲染模板代码。
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
    /**
     * 分页查询已导入的代码生成表列表。
     *
     * @param queryDTO 分页与筛选条件
     * @return 代码生成表 {@link GenTableVO} 分页结果
     */
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
     * 分页查询数据库中尚未导入的物理表列表。
     *
     * @param queryDTO 分页与筛选条件
     * @return 数据库表 {@link GenTableVO} 分页结果
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
     * 批量删除代码生成表及其关联列配置。
     *
     * @param tableIds 待删除的生成表主键 ID 列表
     */
    @Override
    @Transactional
    public void deleteGenTableByIds(List<Long> tableIds)
    {
        genTableMapper.deleteGenTableByIds(tableIds);
        genTableColumnMapper.deleteGenTableColumnByIds(tableIds);
    }
    /**
     * 预览指定表的生成代码（不落盘）。
     *
     * @param tableId 生成表主键 ID
     * @return 模板相对路径到生成内容的映射
     * @throws BizException 生成表不存在时抛出
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
     * 按表名生成代码并打包为 ZIP 字节数组。
     *
     * @param tableName 生成表名称
     * @return ZIP 压缩包字节内容
     * @throws BizException 表不存在或压缩失败时抛出
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
     * 将数据库物理表结构同步至代码生成表列配置。
     * <p>
     * 新增列写入配置，已有列保留查询/表单相关选项，物理表已删除的列会从配置中移除。
     *
     * @param tableName 生成表名称
     * @throws BizException 生成表不存在、列列表为空或原表不存在时抛出
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
     * 批量按表名生成代码并合并为一个 ZIP 压缩包。
     *
     * @param tableNames 生成表名称数组
     * @return ZIP 压缩包字节内容
     * @throws BizException 压缩过程失败时抛出
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
     * 查询单表元数据并将生成文件写入 ZIP 输出流。
     *
     * @param tableName 生成表名称
     * @param zip ZIP 输出流
     * @throws IOException 写入 ZIP 条目失败时抛出
     * @throws BizException 生成表不存在时抛出
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
     * 查询所有已导入的代码生成表。
     *
     * @return 生成表实体列表
     */
    @Override
    public List<GenTable> selectGenTableAll()
    {
        return genTableMapper.selectGenTableAll();
    }
    /**
     * 按主键查询代码生成表详情，并解析扩展选项 JSON。
     *
     * @param id 生成表主键 ID
     * @return 含列配置与树表/菜单选项的 {@link GenTable}
     */
    @Override
    public GenTable selectGenTableById(Long id)
    {
        GenTable genTable = genTableMapper.selectGenTableById(id);
        setTableFromOptions(genTable);
        return genTable;
    }
    /**
     * 执行 DDL 语句在数据库中创建物理表。
     *
     * @param sql CREATE TABLE 等 DDL 语句
     * @return {@code true} 表示执行成功（影响行数为 0）
     */
    @Override
    public boolean createTable(String sql) {
        return genTableMapper.createTable(sql) == 0;
    }
    /**
     * 按表名批量查询数据库物理表元数据。
     *
     * @param tableNames 表名称数组
     * @return 匹配的 {@link GenTable} 列表
     */
    @Override
    public List<GenTable> selectDbTableListByNames(String[] tableNames)
    {
        return genTableMapper.selectDbTableListByNames(tableNames);
    }
    /**
     * 将数据库物理表导入为代码生成表并初始化列配置。
     *
     * @param tableList 待导入的表元数据列表
     * @param operName 操作人员名称，用于填充创建人字段
     * @throws BizException 导入过程发生异常时抛出
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
     * 更新代码生成表主表信息及其列配置。
     *
     * @param genTable 含最新列配置的生成表实体
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
     * 校验树表/主子表模板所需的扩展参数是否完整。
     *
     * @param genTable 待保存的生成表实体
     * @throws BizException 必填项缺失时抛出
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
     * 将 {@link GenTable} 实体转换为列表展示用 {@link GenTableVO}。
     *
     * @param genTable 生成表实体
     * @return 转换后的 VO 对象
     */
    private GenTableVO convertToVO(GenTable genTable) {
        GenTableVO vo = new GenTableVO();
        BeanUtils.copyProperties(genTable, vo);
        return vo;
    }
    /**
     * 解析并设置生成表的主键列引用，供模板渲染使用。
     *
     * @param table 生成表实体
     */
    public void setPkColumn(GenTable table)
    {
        GenTableCodegenHelper.setPkColumn(table);
    }

    /**
     * 加载并挂载主子表模板所需的关联子表实体。
     *
     * @param table 主表生成表实体
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
     * 从 options JSON 解析树表编码、父菜单等扩展配置并回填至实体字段。
     *
     * @param genTable 含 options 字段的生成表实体
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

