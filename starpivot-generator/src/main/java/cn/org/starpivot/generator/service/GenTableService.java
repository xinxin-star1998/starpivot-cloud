package cn.org.starpivot.generator.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.generator.domain.bo.GenTableVO;
import cn.org.starpivot.generator.domain.dto.GenTableQueryDTO;
import cn.org.starpivot.generator.domain.entity.GenTable;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 代码生成表服务接口
 *
 * @author xinxin
 * @since 2025-01-17
 */
public interface GenTableService extends IService<GenTable> {

    /**
     * 分页查询代码生成表列表
     *
     * @param queryDTO 查询条件
     * @return 代码生成表分页列表
     */
    PageResponse<GenTableVO> selectGenTablePage(GenTableQueryDTO queryDTO);
    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    List<GenTable> selectGenTableAll();

    /**
     * 查询业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    GenTable selectGenTableById(Long id);
    /**
     * 创建表
     *
     * @param sql 创建表语句
     * @return 结果
     */
    boolean createTable(String sql);
    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @return 数据库表集合
     */
    List<GenTable> selectDbTableListByNames(String[] tableNames);
    /**
     * 导入表结构
     *
     * @param tableList 导入表列表
     * @param operName 操作人员
     */
    void importGenTable(List<GenTable> tableList, String operName);
    /**
     * 修改保存参数校验
     *
     * @param genTable 业务信息
     */
    void validateEdit(GenTable genTable);
    /**
     * 修改业务
     *
     * @param genTable 业务信息
     */
    void updateGenTable(GenTable genTable);
    /**
     * 查询数据库列表
     *
     * @param queryDTO 查询条件
     * @return 数据库表集合
     */
    PageResponse<GenTableVO> selectDbTableList(GenTableQueryDTO queryDTO);

    /**
     * 删除业务信息
     *
     * @param tableIds 需要删除的表数据ID
     */
     void deleteGenTableByIds(List<Long> tableIds);

    /**
     * 预览代码
     *
     * @param tableId 表编号
     * @return 预览数据列表
     */
    Map<String, String> previewCode(Long tableId);
    /**
     * 生成代码（下载方式）
     *
     * @param tableName 表名称
     * @return 数据
     */
    byte[] downloadCode(String tableName);
    /**
     * 同步数据库
     *
     * @param tableName 表名称
     */
    void synchDb(String tableName);
    /**
     * 批量生成代码（下载方式）
     *
     * @param tableNames 表数组
     * @return 数据
     */
    byte[] downloadCode(String[] tableNames);
}

