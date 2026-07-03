package cn.org.starpivot.generator.service.support;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.external.GenTableCodegenHelper;
import cn.org.starpivot.generator.mapper.GenTableMapper;
import cn.org.starpivot.generator.utils.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 代码生成表元数据解析与校验。
 */
@Component
@RequiredArgsConstructor
public class GenTableMetadataSupport {

    private final GenTableMapper genTableMapper;

    public void prepareForCodegen(GenTable table) {
        attachSubTable(table);
        GenTableCodegenHelper.setPkColumn(table);
    }

    public void attachSubTable(GenTable table) {
        String subTableName = table.getSubTableName();
        if (StringUtils.isNotEmpty(subTableName)) {
            table.setSubTable(genTableMapper.selectGenTableByName(subTableName));
        }
    }

    public void applyOptionsFromJson(GenTable genTable) {
        String options = genTable.getOptions();
        if (StringUtils.isEmpty(options)) {
            return;
        }
        JSONObject paramsObj = JSON.parseObject(options);
        if (StringUtils.isNotNull(paramsObj)) {
            genTable.setTreeCode(paramsObj.getString(GenConstants.TREE_CODE));
            genTable.setTreeParentCode(paramsObj.getString(GenConstants.TREE_PARENT_CODE));
            genTable.setTreeName(paramsObj.getString(GenConstants.TREE_NAME));
            genTable.setParentMenuId(paramsObj.getLongValue(GenConstants.PARENT_MENU_ID));
            genTable.setParentMenuName(paramsObj.getString(GenConstants.PARENT_MENU_NAME));
        }
    }

    public void validateEdit(GenTable genTable) {
        if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
            String options = JSON.toJSONString(genTable.getParams());
            JSONObject paramsObj = JSON.parseObject(options);
            if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_CODE))) {
                throw new BizException("树编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_PARENT_CODE))) {
                throw new BizException("树父编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_NAME))) {
                throw new BizException("树名称字段不能为空");
            }
        } else if (GenConstants.TPL_SUB.equals(genTable.getTplCategory())) {
            if (StringUtils.isEmpty(genTable.getSubTableName())) {
                throw new BizException("关联子表的表名不能为空");
            } else if (StringUtils.isEmpty(genTable.getSubTableFkName())) {
                throw new BizException("子表关联的外键名不能为空");
            }
        }
    }
}
