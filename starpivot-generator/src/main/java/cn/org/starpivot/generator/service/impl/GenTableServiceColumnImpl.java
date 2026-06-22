package cn.org.starpivot.generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.mapper.GenTableColumnMapper;
import cn.org.starpivot.generator.service.GenTableColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代码生成表列配置服务实现。
 * <p>
 * 实现 {@link GenTableColumnService}，按生成表 ID 查询已保存的列元数据与表单配置。
 */
@Service
public class GenTableServiceColumnImpl extends ServiceImpl<GenTableColumnMapper, GenTableColumn> implements GenTableColumnService {
    @Autowired
    private GenTableColumnMapper genTableColumnMapper;
    /**
     * 按生成表 ID 查询列配置列表。
     *
     * @param tableId 生成表主键 ID
     * @return {@link GenTableColumn} 列配置列表
     */
    @Override
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId)
    {
        return genTableColumnMapper.selectGenTableColumnListByTableId(tableId);
    }
}

