/**
 * @deprecated 旧版通用导入导出（前端 XLSX 解析 + `POST /common/import-export/*`）已移除。
 *
 * 请改用 EasyExcel 方案：
 * - {@link ./excel.ts} — `fetchExcelExport` / `fetchExcelImport` / `fetchExcelTemplate`
 * - {@link @/components/core/forms/excel-import-dialog} — 导入弹窗
 * - 业务 API 示例：`@/api/user/user` 的 `fetchExportUser`、`fetchImportUserExcel`
 */

export type { ExcelImportResultVo } from './excel'
