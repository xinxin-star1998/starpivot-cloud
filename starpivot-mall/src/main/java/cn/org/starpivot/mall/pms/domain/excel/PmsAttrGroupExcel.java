package cn.org.starpivot.mall.pms.domain.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 属性分组 Excel 行（EasyExcel 导入导出）
 */
@Data
public class PmsAttrGroupExcel {

    @ExcelProperty(value = "分组ID", index = 0)
    private Long attrGroupId;

    @ExcelProperty(value = "组名", index = 1)
    private String attrGroupName;

    @ExcelProperty(value = "排序", index = 2)
    private Long sort;

    @ExcelProperty(value = "描述", index = 3)
    private String descript;

    @ExcelProperty(value = "组图标", index = 4)
    private String icon;

    @ExcelProperty(value = "所属分类ID", index = 5)
    private Long catelogId;
}
