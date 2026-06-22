package cn.org.starpivot.system.domain.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 用户 Excel 导入导出模型。
 * <p>
 * 定义用户数据的 Excel 列映射，用于批量导入与导出。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link ExcelProperty} — 指定 Excel 列标题与列序号</li>
 * </ul>
 */
@Data
public class SysUserExcel {

    /**
     * 用户账号
     */
    @ExcelProperty(value = "用户账号", index = 0)
    private String userName;

    /**
     * 用户昵称
     */
    @ExcelProperty(value = "用户昵称", index = 1)
    private String nickName;

    /**
     * 用户邮箱
     */
    @ExcelProperty(value = "邮箱", index = 2)
    private String email;

    /**
     * 手机号码
     */
    @ExcelProperty(value = "手机号", index = 3)
    private String phonenumber;

    /**
     * 用户性别
     */
    @ExcelProperty(value = "性别", index = 4)
    private String sex;

    /**
     * 帐号状态
     */
    @ExcelProperty(value = "状态", index = 5)
    private String status;

    /**
     * 部门ID
     */
    @ExcelProperty(value = "部门ID", index = 6)
    private Long deptId;

    /**
     * 部门名称
     */
    @ExcelProperty(value = "部门名称", index = 7)
    private String deptName;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 8)
    private String remark;
}
