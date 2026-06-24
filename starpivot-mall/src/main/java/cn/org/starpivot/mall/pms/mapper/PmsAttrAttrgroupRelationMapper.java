package cn.org.starpivot.mall.pms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.org.starpivot.mall.pms.entity.PmsAttrAttrgroupRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 属性 ↔ 属性分组关联表 Mapper（{@code pms_attr_attrgroup_relation}）。
 */
@Mapper
public interface PmsAttrAttrgroupRelationMapper extends BaseMapper<PmsAttrAttrgroupRelation> {}
