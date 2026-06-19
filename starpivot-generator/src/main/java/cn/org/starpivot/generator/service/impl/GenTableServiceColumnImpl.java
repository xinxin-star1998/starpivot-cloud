package cn.org.starpivot.generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;
import cn.org.starpivot.generator.mapper.GenTableColumnMapper;
import cn.org.starpivot.generator.service.GenTableColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 代码生成表服务实现类
 *
 * @author xinxin
 * @since 2025-01-17
 */
@Service
public class GenTableServiceColumnImpl extends ServiceImpl<GenTableColumnMapper, GenTableColumn> implements GenTableColumnService {
    @Autowired
    private GenTableColumnMapper genTableColumnMapper;
    /**
     * 查询业务字段列表
     *
     * @param tableId 业务字段编号
     * @return 业务字段集合
     */
    @Override
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId)
    {
        return genTableColumnMapper.selectGenTableColumnListByTableId(tableId);
    }
}

