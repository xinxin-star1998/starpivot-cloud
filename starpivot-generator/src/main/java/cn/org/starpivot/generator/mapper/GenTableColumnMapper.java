package cn.org.starpivot.generator.mapper;

import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 代码生成表Mapper接口
 *
 * @author xinxin
 * @since 2025-01-17
 */
@Mapper
public interface GenTableColumnMapper extends BaseMapper<GenTableColumn> {
    /**
     * 查询业务字段列表
     *
     * @param tableId 业务字段编号
     * @return 业务字段集合
     */
    List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);
    /**
     * 根据表名称查询列信息
     *
     * @param tableName 表名称
     * @return 列信息
     */
    List<GenTableColumn> selectDbTableColumnsByName(String tableName);
    /**
     * 新增业务字段
     *
     * @param genTableColumn 业务字段信息
     * @return 结果
     */
    int insertGenTableColumn(GenTableColumn genTableColumn);
    /**
     * 修改业务字段
     *
     * @param genTableColumn 业务字段信息
     * @return 结果
     */
    int updateGenTableColumn(GenTableColumn genTableColumn);
    /**
     * 批量删除业务字段
     *
     * @param tableIds 需要删除的数据ID
     * @return 结果
     */
    int deleteGenTableColumnByIds(List<Long> tableIds);

    void deleteGenTableColumns(List<GenTableColumn> delColumns);
}

