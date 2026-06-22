package cn.org.starpivot.system.excel;

import cn.org.starpivot.common.excel.ExcelBizHandler;
import cn.org.starpivot.common.excel.ExcelImportOptions;
import cn.org.starpivot.common.excel.ExcelImportResult;
import cn.org.starpivot.system.domain.bo.UserReqBo;
import cn.org.starpivot.system.domain.excel.SysUserExcel;
import cn.org.starpivot.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户 Excel 业务处理器。
 * <p>
 * 实现 {@link ExcelBizHandler}，对接通用 Excel 导入导出框架，委托 {@link SysUserService} 完成数据读写。
 * </p>
 * <ul>
 *   <li>{@link Component} — 注册为 Spring Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 为 final 字段生成构造器注入</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class SysUserExcelHandler implements ExcelBizHandler<SysUserExcel, UserReqBo> {

    /**
     * 用户业务服务
     */
    private final SysUserService sysUserService;

    /**
     * 按查询条件导出用户数据。
     *
     * @param query 用户查询条件
     * @return 待写入 Excel 的用户行数据
     */
    @Override
    public List<SysUserExcel> listForExport(UserReqBo query) {
        return sysUserService.listForExport(query);
    }

    /**
     * 批量导入用户数据。
     *
     * @param rows    Excel 解析后的用户行
     * @param options 导入选项（如是否支持更新已存在用户）
     * @return 导入结果（成功/失败条数及错误明细）
     */
    @Override
    public ExcelImportResult importRows(List<SysUserExcel> rows, ExcelImportOptions options) {
        return sysUserService.importFromExcel(rows, options.isUpdateSupport());
    }

    /**
     * 返回导出工作表名称。
     *
     * @param query 用户查询条件
     * @return 工作表名称
     */
    @Override
    public String sheetName(UserReqBo query) {
        return "用户";
    }

    /**
     * 生成导出文件名。
     *
     * @param query 用户查询条件
     * @return 带时间戳的 xlsx 文件名
     */
    @Override
    public String exportFileName(UserReqBo query) {
        return "user_export_" + System.currentTimeMillis() + ".xlsx";
    }

    /**
     * 返回导入模板文件名。
     *
     * @param query 用户查询条件
     * @return 模板文件名
     */
    @Override
    public String templateFileName(UserReqBo query) {
        return "user_import_template.xlsx";
    }

    /**
     * 提供导入模板的示例数据行。
     *
     * @param query 用户查询条件
     * @return 示例用户行列表
     */
    @Override
    public List<SysUserExcel> templateSampleRows(UserReqBo query) {
        SysUserExcel sample = new SysUserExcel();
        sample.setUserName("test001");
        sample.setNickName("测试用户");
        sample.setEmail("test@example.com");
        sample.setPhonenumber("13800138000");
        sample.setSex("男");
        sample.setStatus("正常");
        sample.setDeptId(1L);
        sample.setRemark("示例数据");
        return List.of(sample);
    }
}
