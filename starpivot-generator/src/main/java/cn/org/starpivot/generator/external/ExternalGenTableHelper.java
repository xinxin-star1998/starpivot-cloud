package cn.org.starpivot.generator.external;

import cn.org.starpivot.common.constants.GenConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.domain.entity.GenTable;
import cn.org.starpivot.generator.domain.external.GenPathProfile;
import cn.org.starpivot.generator.utils.StringUtils;
import com.alibaba.fastjson2.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * 外部库生成草稿：options 同步与配置校验
 */
public final class ExternalGenTableHelper {

    /** 表级路径覆盖（params 键） */
    public static final String PARAM_VUE_PAGE_PATH = "vuePagePath";
    public static final String PARAM_API_PATH = "apiPath";

    private ExternalGenTableHelper() {
    }

    /**
     * 将树表/菜单等扩展字段写入 options，供 Velocity 模板读取
     */
    public static void syncOptions(GenTable table) {
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotEmpty(table.getTreeCode())) {
            params.put(GenConstants.TREE_CODE, table.getTreeCode());
        }
        if (StringUtils.isNotEmpty(table.getTreeParentCode())) {
            params.put(GenConstants.TREE_PARENT_CODE, table.getTreeParentCode());
        }
        if (StringUtils.isNotEmpty(table.getTreeName())) {
            params.put(GenConstants.TREE_NAME, table.getTreeName());
        }
        if (table.getParentMenuId() != null) {
            params.put(GenConstants.PARENT_MENU_ID, table.getParentMenuId());
        }
        if (StringUtils.isNotEmpty(table.getParentMenuName())) {
            params.put(GenConstants.PARENT_MENU_NAME, table.getParentMenuName());
        }
        table.setOptions(params.isEmpty() ? null : JSON.toJSONString(params));
    }

    public static void validateGenConfig(GenTable table) {
        if (GenConstants.TPL_TREE.equals(table.getTplCategory())) {
            if (StringUtils.isEmpty(table.getTreeCode())) {
                throw new BizException("表 " + table.getTableName() + "：树编码字段不能为空");
            }
            if (StringUtils.isEmpty(table.getTreeParentCode())) {
                throw new BizException("表 " + table.getTableName() + "：树父编码字段不能为空");
            }
            if (StringUtils.isEmpty(table.getTreeName())) {
                throw new BizException("表 " + table.getTableName() + "：树名称字段不能为空");
            }
        } else if (GenConstants.TPL_SUB.equals(table.getTplCategory())) {
            if (StringUtils.isEmpty(table.getSubTableName())) {
                throw new BizException("表 " + table.getTableName() + "：关联子表的表名不能为空");
            }
            if (StringUtils.isEmpty(table.getSubTableFkName())) {
                throw new BizException("表 " + table.getTableName() + "：子表关联的外键名不能为空");
            }
        }
    }

    /**
     * 合并表级 vue/api 路径覆盖到会话 pathProfile 副本
     */
    public static GenPathProfile resolveEffectiveProfile(GenTable table, GenPathProfile base) {
        if (base == null) {
            return null;
        }
        GenPathProfile profile = base.copy();
        if (table == null || table.getParams() == null) {
            return profile;
        }
        Object vue = table.getParams().get(PARAM_VUE_PAGE_PATH);
        if (vue != null && StringUtils.isNotEmpty(String.valueOf(vue))) {
            profile.setVuePagePath(String.valueOf(vue));
            profile.setVueModulesPath(null);
        }
        Object api = table.getParams().get(PARAM_API_PATH);
        if (api != null && StringUtils.isNotEmpty(String.valueOf(api))) {
            profile.setApiPath(String.valueOf(api));
        }
        return profile;
    }
}

