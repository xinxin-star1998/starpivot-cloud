import request from '@/utils/http'

/** 外部库连接参数 */
export interface ExternalDbConnection {
  dbType?: string
  schema?: string
  /** Oracle：service / sid / tns */
  oracleConnectMode?: string
  host: string
  port: number
  database: string
  username: string
  password: string
  params?: string
}

/** 分层路径配置 */
export interface GenPathProfile {
  basePackage: string
  entityPackage?: string
  dtoPackage?: string
  voPackage?: string
  boPackage?: string
  mapperPackage?: string
  servicePackage?: string
  serviceImplPackage?: string
  controllerPackage?: string
  mapperXmlPath?: string
  apiPath?: string
  vuePagePath?: string
  vueModulesPath?: string
}

export interface ExternalConnectResult {
  sessionId: string
  database: string
  dbVersion: string
  expireMinutes: number
}

export interface ExternalTableItem {
  tableName: string
  tableComment?: string
  createTime?: string
  updateTime?: string
}

export interface ExternalTablePage {
  total: number
  rows: ExternalTableItem[]
  pageNum: number
  pageSize: number
  pageCount: number
}

export interface ExternalGenScope {
  genBackend?: boolean
  genFrontend?: boolean
  genSql?: boolean
}

export interface ExternalImportResult {
  imported: string[]
  updated: string[]
  skipped: string[]
}

export interface GenTableColumnItem {
  columnId?: number
  columnName: string
  columnComment?: string
  columnType?: string
  javaType?: string
  javaField?: string
  isPk?: string
  isIncrement?: string
  isRequired?: string
  isInsert?: string
  isEdit?: string
  isList?: string
  isQuery?: string
  queryType?: string
  htmlType?: string
  dictType?: string
  sort?: number
}

export function fetchExternalConnect(connection: ExternalDbConnection) {
  return request.post<ExternalConnectResult>({
    url: '/api/tool/gen/external/connect',
    data: connection
  })
}

export function fetchExternalDisconnect(sessionId: string) {
  return request.del<void>({
    url: `/api/tool/gen/external/disconnect/${sessionId}`
  })
}

export interface ExternalSessionStatus {
  sessionId: string
  database: string
  dbVersion?: string
  remainingSeconds: number
  expireMinutes: number
  templateDir?: string
  effectiveTemplateDir?: string
}

export function fetchExternalSessionStatus(sessionId: string) {
  return request.get<ExternalSessionStatus>({
    url: `/api/tool/gen/external/session/${sessionId}`
  })
}

export function fetchExternalTables(params: {
  sessionId: string
  pageNum: number
  pageSize: number
  tableName?: string
  tableComment?: string
}) {
  return request.post<ExternalTablePage>({
    url: '/api/tool/gen/external/tables',
    data: params
  })
}

export function fetchExternalColumns(sessionId: string, tableName: string) {
  return request.post<GenTableColumnItem[]>({
    url: '/api/tool/gen/external/columns',
    data: { sessionId, tableName }
  })
}

export function fetchExternalSavePathProfile(sessionId: string, pathProfile: GenPathProfile) {
  return request.put<void>({
    url: '/api/tool/gen/external/path-profile',
    data: { sessionId, pathProfile }
  })
}

export function fetchExternalSaveDraft(payload: {
  sessionId: string
  tableName: string
  tableComment?: string
  className?: string
  businessName?: string
  functionName?: string
  treeCode?: string
  treeParentCode?: string
  treeName?: string
  parentMenuId?: number | string
  subTableName?: string
  subTableFkName?: string
  vuePagePath?: string
  apiPath?: string
  columns?: GenTableColumnItem[]
}) {
  return request.put<void>({
    url: '/api/tool/gen/external/draft',
    data: payload
  })
}

export function fetchExternalSaveGenConfig(payload: {
  sessionId: string
  tableNames: string[]
  tplCategory: string
  tplWebType: string
  author?: string
  pathProfile?: GenPathProfile
}) {
  return request.put<void>({
    url: '/api/tool/gen/external/gen-config',
    data: payload
  })
}

export function fetchExternalPreview(
  sessionId: string,
  tableName: string,
  genScope?: ExternalGenScope
) {
  return request.post<Record<string, string>>({
    url: '/api/tool/gen/external/preview',
    data: { sessionId, tableName, genScope }
  })
}

export function fetchExternalDownload(
  sessionId: string,
  tableNames: string[],
  genScope?: ExternalGenScope
) {
  return request.post<Blob>({
    url: '/api/tool/gen/external/download',
    data: { sessionId, tableNames, genScope },
    responseType: 'blob'
  })
}

export function fetchExternalImportGenTable(
  sessionId: string,
  tableNames: string[],
  overwrite?: boolean
) {
  return request.post<ExternalImportResult>({
    url: '/api/tool/gen/external/import-gen-table',
    data: { sessionId, tableNames, overwrite }
  })
}

export interface ExternalWriteResult {
  outputRoot: string
  writtenFiles: string[]
  fileCount: number
  backupId?: string
  backedUpCount?: number
}

export interface ExternalWriteDirEntry {
  name: string
  path: string
}

export interface ExternalWriteDirList {
  current?: string
  parent?: string
  directories: ExternalWriteDirEntry[]
}

export interface ExternalWriteRollbackResult {
  backupId: string
  outputRoot: string
  restoredFiles: string[]
  fileCount: number
}

export type ExternalWriteDiffStatus = 'NEW' | 'MODIFIED' | 'UNCHANGED'

export interface ExternalWriteDiffItem {
  path: string
  status: ExternalWriteDiffStatus
  oldContent?: string | null
  newContent?: string | null
}

export interface ExternalWriteDiffResult {
  outputRoot: string
  newCount: number
  modifiedCount: number
  unchangedCount: number
  files: ExternalWriteDiffItem[]
}

export function fetchExternalWriteToDisk(
  sessionId: string,
  tableNames: string[],
  outputRoot: string | undefined,
  genScope?: ExternalGenScope,
  options?: { onlyChanged?: boolean; selectedPaths?: string[]; backupBeforeWrite?: boolean }
) {
  return request.post<ExternalWriteResult>({
    url: '/api/tool/gen/external/write',
    data: {
      sessionId,
      tableNames,
      outputRoot: outputRoot || undefined,
      genScope,
      onlyChanged: options?.onlyChanged,
      selectedPaths: options?.selectedPaths,
      backupBeforeWrite: options?.backupBeforeWrite
    }
  })
}

export function fetchExternalWriteDiff(
  sessionId: string,
  tableNames: string[],
  outputRoot: string | undefined,
  genScope?: ExternalGenScope,
  includeContent = true
) {
  return request.post<ExternalWriteDiffResult>({
    url: '/api/tool/gen/external/write-diff',
    data: {
      sessionId,
      tableNames,
      outputRoot: outputRoot || undefined,
      genScope,
      includeContent
    }
  })
}

export function fetchExternalWriteDiffFile(
  sessionId: string,
  tableNames: string[],
  path: string,
  outputRoot: string | undefined,
  genScope?: ExternalGenScope
) {
  return request.post<ExternalWriteDiffItem>({
    url: '/api/tool/gen/external/write-diff/file',
    data: { sessionId, tableNames, path, outputRoot: outputRoot || undefined, genScope }
  })
}

export function fetchExternalSaveTemplateDir(sessionId: string, templateDir: string) {
  return request.put<void>({
    url: '/api/tool/gen/external/template-dir',
    data: { sessionId, templateDir }
  })
}

export function fetchExternalWriteDirs(path?: string) {
  return request.get<ExternalWriteDirList>({
    url: '/api/tool/gen/external/write-dirs',
    params: path ? { path } : undefined
  })
}

export function fetchExternalWriteRollback(
  sessionId: string,
  backupId: string,
  outputRoot?: string
) {
  return request.post<ExternalWriteRollbackResult>({
    url: '/api/tool/gen/external/write-rollback',
    data: { sessionId, backupId, outputRoot: outputRoot || undefined }
  })
}

export function fetchExternalCapabilities() {
  return request.get<{
    dbTypes: { value: string; label: string; defaultPort: number }[]
    oracleConnectModes: { value: string; label: string }[]
    writeToDiskEnabled: boolean
    backupBeforeWrite?: boolean
    globalTemplateDir?: string
    defaultOutputRoot?: string
    maxTablesPerBatch: number
  }>({
    url: '/api/tool/gen/external/capabilities'
  })
}

export function fetchExternalTemplates() {
  return request.get<{
    categories: { value: string; label: string }[]
    webTypes: { value: string; label: string }[]
  }>({
    url: '/api/tool/gen/external/templates'
  })
}
