package cn.org.starpivot.system.domain.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class SysUserExcel {
    @ExcelProperty(value = "用户账号", index = 0)
    private String userName;
    @ExcelProperty(value = "用户昵称", index = 1)
    private String nickName;
    @ExcelProperty(value = "邮箱", index = 2)
    private String email;
    @ExcelProperty(value = "手机号", index = 3)
    private String phonenumber;
    @ExcelProperty(value = "性别", index = 4)
    private String sex;
    @ExcelProperty(value = "状态", index = 5)
    private String status;
    @ExcelProperty(value = "部门ID", index = 6)
    private Long deptId;
    @ExcelProperty(value = "部门名称", index = 7)
    private String deptName;
    @ExcelProperty(value = "备注", index = 8)
    private String remark;
}
