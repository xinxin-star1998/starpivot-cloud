package cn.org.starpivot.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.org.starpivot.generator.domain.entity.GenTableColumn;

import java.util.List;

/**
 * 代码生成表服务接口
 *
 * @author xinxin
 * @since 2025-01-17
 */
public interface GenTableColumnService extends IService<GenTableColumn> {
    /**
     * 查询业务字段列表
     *
     * @param tableId 业务字段编号
     * @return 业务字段集合
     */
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);
}

