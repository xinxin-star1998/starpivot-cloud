package cn.org.starpivot.mall.pms.domain.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 商品属性 Excel 行（EasyExcel 导入导出）
 */
@Data
public class PmsAttrExcel {

    @ExcelProperty(value = "属性ID", index = 0)
    private Long attrId;

    @ExcelProperty(value = "属性名", index = 1)
    private String attrName;

    @ExcelProperty(value = "检索(0否1是)", index = 2)
    private Integer searchType;

    @ExcelProperty(value = "值类型(0单1多)", index = 3)
    private Integer valueType;

    @ExcelProperty(value = "图标", index = 4)
    private String icon;

    @ExcelProperty(value = "可选值(分号分隔)", index = 5)
    private String valueSelect;

    @ExcelProperty(value = "启用(0禁1启)", index = 6)
    private Long enable;

    @ExcelProperty(value = "分类ID", index = 7)
    private Long catelogId;

    @ExcelProperty(value = "快速展示(0否1是)", index = 8)
    private Integer showDesc;

    @ExcelProperty(value = "属性分组ID", index = 9)
    private Long attrGroupId;

    @ExcelProperty(value = "组内排序", index = 10)
    private Integer attrSort;
}
