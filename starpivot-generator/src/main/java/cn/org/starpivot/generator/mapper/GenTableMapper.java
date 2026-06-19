package cn.org.starpivot.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.dto.GenTableQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代码生成表Mapper接口
 *
 * @author xinxin
 * @since 2025-01-17
 */
@Mapper
public interface GenTableMapper extends BaseMapper<GenTable> {
    /**
     * 创建表
     *
     * @param sql 表结构
     * @return 结果
     */
    int createTable(@Param("sql") String sql);

    List<GenTable> selectDbTableListByNames(String[] tableNames);
    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    List<GenTable> selectGenTableAll();

    /**
     * 查询表ID业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    GenTable selectGenTableById(Long id);
    /**
     * 新增业务
     *
     * @param genTable 业务信息
     * @return 结果
     */
    int insertGenTable(GenTable genTable);

    /**
     * 分页查询代码生成表列表（XML方式）
     *
     * @param page     分页参数
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<GenTable> selectGenTableList(Page<?> page, @Param("query") GenTableQueryDTO queryDTO);
    /**
     * 修改业务
     *
     * @param genTable 业务信息
     * @return 结果
     */
    int updateGenTable(GenTable genTable);

    IPage<GenTable> selectDbTableList(Page<GenTable> page, @Param("query") GenTableQueryDTO queryDTO);

    /**
     * 批量删除业务
     *
     * @param tableIds 需要删除的数据
     * @return 结果
     */
    int deleteGenTableByIds(List<Long> tableIds);
    /**
     * 查询表名称业务信息
     *
     * @param tableName 表名称
     * @return 业务信息
     */
    GenTable selectGenTableByName(String tableName);
}

