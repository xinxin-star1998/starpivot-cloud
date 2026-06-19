<template>
  <div class="gen-external-page art-full-height">
    <ElCard shadow="never" class="step-card">
      <template #header>
        <div class="card-header">
          <span class="title">外部库代码生成</span>
          <ElSpace>
            <ElTag v-if="sessionId" type="success" effect="plain">
              已连接 · {{ dbInfo }}
              <span v-if="sessionRemainingText" class="session-ttl">
                · {{ sessionRemainingText }}</span
              >
            </ElTag>
            <ExternalActionBtn
              v-if="sessionId"
              what="结束当前会话"
              usage="断开 JDBC 连接并清除会话缓存。未完成的工作请先下载 ZIP 或写盘。"
              link
              type="danger"
              @click="handleDisconnect"
            >
              断开连接
            </ExternalActionBtn>
          </ElSpace>
        </div>
      </template>

      <ElSteps :active="currentStep" finish-status="success" align-center class="mb-6">
        <ElStep title="连接数据库" />
        <ElStep title="选择数据表" />
        <ElStep title="生成模板" />
        <ElStep title="路径配置" />
        <ElStep title="预览与下载" />
      </ElSteps>

      <!-- Step 0: 连接 -->
      <div v-show="currentStep === 0" class="step-panel">
        <ElForm label-width="100px" class="conn-preset-bar mb-4">
          <ElRow :gutter="16">
            <ElCol :span="8">
              <ElFormItem label="连接预设">
                <ElSelect
                  v-model="selectedConnPreset"
                  placeholder="我的连接"
                  clearable
                  class="w-full"
                  @change="applyConnPreset"
                >
                  <ElOption
                    v-for="p in connPresets"
                    :key="p.name"
                    :label="p.name"
                    :value="p.name"
                  />
                </ElSelect>
              </ElFormItem>
            </ElCol>
            <ElCol :span="10">
              <ElFormItem label="保存预设">
                <ElInput v-model="newConnPresetName" placeholder="名称" maxlength="32" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="6">
              <ElFormItem label-width="0">
                <ElCheckbox v-model="saveConnPassword">记住密码</ElCheckbox>
                <ExternalActionBtn
                  what="存到浏览器"
                  usage="将当前连接参数保存为预设，下次可快速填充。勾选「记住密码」会本地保存密码。"
                  link
                  type="primary"
                  class="ml-2"
                  @click="saveConnPreset"
                >
                  保存
                </ExternalActionBtn>
                <ExternalActionBtn
                  v-if="selectedConnPreset"
                  what="删预设"
                  usage="从浏览器本地存储删除当前选中的连接预设，不影响已连接会话。"
                  link
                  type="danger"
                  @click="deleteConnPreset"
                >
                  删除
                </ExternalActionBtn>
              </ElFormItem>
            </ElCol>
          </ElRow>
        </ElForm>
        <ElForm
          ref="connFormRef"
          :model="connection"
          :rules="connRules"
          label-width="100px"
          class="conn-form"
        >
          <ElRow :gutter="16">
            <ElCol :span="8">
              <ElFormItem label="数据库类型">
                <ElSelect v-model="connection.dbType" class="w-full" @change="onDbTypeChange">
                  <ElOption label="MySQL" value="mysql" />
                  <ElOption label="PostgreSQL" value="postgresql" />
                  <ElOption label="Oracle" value="oracle" />
                  <ElOption label="SQL Server" value="sqlserver" />
                </ElSelect>
              </ElFormItem>
            </ElCol>
            <ElCol :span="8">
              <ElFormItem label="主机" prop="host">
                <ElInput v-model="connection.host" placeholder="127.0.0.1" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="8">
              <ElFormItem label="端口" prop="port">
                <ElInputNumber v-model="connection.port" :min="1" :max="65535" class="w-full" />
              </ElFormItem>
            </ElCol>
            <ElCol v-if="connection.dbType === 'oracle'" :span="8">
              <ElFormItem label="连接模式">
                <ElSelect v-model="connection.oracleConnectMode" class="w-full">
                  <ElOption label="Service（含 RAC SCAN）" value="service" />
                  <ElOption label="SID" value="sid" />
                  <ElOption label="TNS 描述符" value="tns" />
                </ElSelect>
              </ElFormItem>
            </ElCol>
            <ElCol :span="8">
              <ElFormItem :label="oracleDatabaseLabel" prop="database">
                <ElInput v-model="connection.database" :placeholder="oracleDatabasePlaceholder" />
              </ElFormItem>
            </ElCol>
            <ElCol
              v-if="['postgresql', 'oracle', 'sqlserver'].includes(connection.dbType || 'mysql')"
              :span="8"
            >
              <ElFormItem label="Schema">
                <ElInput v-model="connection.schema" :placeholder="schemaPlaceholder" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="8">
              <ElFormItem label="用户名" prop="username">
                <ElInput v-model="connection.username" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="8">
              <ElFormItem label="密码" prop="password">
                <ElInput v-model="connection.password" type="password" show-password />
              </ElFormItem>
            </ElCol>
            <ElCol :span="16">
              <ElFormItem label="JDBC 参数">
                <ElInput v-model="connection.params" :placeholder="jdbcParamsPlaceholder" />
              </ElFormItem>
            </ElCol>
          </ElRow>
        </ElForm>
      </div>

      <!-- Step 1: 选表 -->
      <div v-show="currentStep === 1" class="step-panel">
        <ElForm :inline="true" :model="tableSearch" class="mb-3">
          <ElFormItem label="表名称">
            <ElInput v-model="tableSearch.tableName" clearable placeholder="模糊搜索" />
          </ElFormItem>
          <ElFormItem label="表描述">
            <ElInput v-model="tableSearch.tableComment" clearable placeholder="模糊搜索" />
          </ElFormItem>
          <ElFormItem>
            <ExternalActionBtn
              what="查外部库表"
              usage="按表名/表描述模糊查询当前连接库中的表（已过滤 qrtz、gen 开头系统表）。"
              type="primary"
              :loading="tableLoading"
              @click="loadTables"
            >
              搜索
            </ExternalActionBtn>
            <ExternalActionBtn
              what="清空条件"
              usage="清除搜索条件并重新加载第一页表列表。"
              @click="resetTableSearch"
            >
              重置
            </ExternalActionBtn>
          </ElFormItem>
        </ElForm>
        <ArtTable
          :loading="tableLoading"
          :data="tableData"
          :columns="tableColumns"
          :pagination="tablePagination"
          @selection-change="onTableSelectionChange"
          @pagination:size-change="onTableSizeChange"
          @pagination:current-change="onTablePageChange"
        />
      </div>

      <!-- Step 2: 模板 -->
      <div v-show="currentStep === 2" class="step-panel">
        <ElForm label-width="120px" class="tpl-form">
          <ElFormItem label="已选表">
            <ElTag v-for="t in selectedTableNames" :key="t" class="mr-2 mb-2">{{ t }}</ElTag>
          </ElFormItem>
          <ElRow :gutter="16">
            <ElCol :span="12">
              <ElFormItem label="生成模板">
                <ElSelect v-model="genConfig.tplCategory" placeholder="请选择">
                  <ElOption
                    v-for="item in templateOptions.categories"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
            </ElCol>
            <ElCol :span="12">
              <ElFormItem label="前端类型">
                <ElSelect v-model="genConfig.tplWebType" placeholder="请选择">
                  <ElOption
                    v-for="item in templateOptions.webTypes"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
            </ElCol>
          </ElRow>
        </ElForm>

        <ElDivider content-position="left">表生成信息（按表）</ElDivider>
        <ElTabs v-model="activeTableTab" type="border-card">
          <ElTabPane v-for="name in selectedTableNames" :key="name" :label="name" :name="name">
            <ExternalTableExtraForm
              :meta="tableMeta[name]"
              :tpl-category="genConfig.tplCategory"
              :columns="tableColumnsMap[name] || []"
              :current-table-name="name"
              :selected-table-names="selectedTableNames"
              :parent-menus="parentMenus"
              :all-columns-map="tableColumnsMap"
            />
            <ExternalColumnTable
              :columns="tableColumnsMap[name] || []"
              :dict-options="dictOptions"
              :max-height="420"
              class="mt-3"
            />
          </ElTabPane>
        </ElTabs>
      </div>

      <!-- Step 3: 路径 -->
      <div v-show="currentStep === 3" class="step-panel">
        <PathConfigForm v-model="pathProfile" v-model:author="genConfig.author" />
      </div>

      <!-- Step 4: 预览下载 -->
      <div v-show="currentStep === 4" class="step-panel">
        <ElForm label-width="100px" class="mb-4">
          <ElFormItem label="生成范围">
            <ElCheckbox v-model="genScope.genBackend">后端（Java + XML）</ElCheckbox>
            <ElCheckbox v-model="genScope.genFrontend">前端（API + Vue）</ElCheckbox>
            <ElCheckbox v-model="genScope.genSql">菜单 SQL</ElCheckbox>
          </ElFormItem>
        </ElForm>
        <ElForm :inline="true" class="mb-4">
          <ElFormItem label="预览表">
            <ElSelect v-model="previewTableName" placeholder="选择表" style="width: 240px">
              <ElOption v-for="t in selectedTableNames" :key="t" :label="t" :value="t" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem>
            <div class="action-btn-row">
              <ExternalActionBtn
                what="弹窗看代码"
                usage="打开对话框，左树右码预览当前选中表将生成的文件。需权限 tool:external:preview。"
                type="primary"
                v-auth="'tool:external:preview'"
                @click="openPreview"
              >
                预览代码
              </ExternalActionBtn>
              <ExternalActionBtn
                what="打包下载"
                usage="按「生成范围」勾选，将所有已选表代码打成 ZIP 下载到本地，不写入项目目录。"
                type="success"
                v-auth="'tool:external:create'"
                :loading="downloading"
                @click="handleDownload"
              >
                下载 ZIP
              </ExternalActionBtn>
              <ExternalActionBtn
                what="进主库配置"
                usage="把外部库表结构导入系统 gen_table，便于在主「代码生成」菜单继续维护。可勾选是否覆盖已有配置。"
                v-auth="'tool:external:create'"
                :loading="importing"
                @click="handleImportGenTable"
              >
                导入代码生成
              </ExternalActionBtn>
            </div>
          </ElFormItem>
        </ElForm>
        <template v-if="writeToDiskEnabled">
          <ElForm :inline="true" class="mb-4">
            <ElFormItem label="写盘根目录">
              <ElAutocomplete
                v-model="outputRoot"
                :fetch-suggestions="queryOutputRoots"
                placeholder="服务端项目绝对路径"
                style="width: 360px"
                clearable
              />
              <ExternalActionBtn
                what="浏览服务端目录"
                usage="写盘在后端服务器执行，此处选择服务器磁盘上的目录（非浏览器本机文件夹）。开发环境前后端同机时即为本地项目路径。"
                class="ml-2"
                @click="outputRootPickerVisible = true"
              >
                浏览目录
              </ExternalActionBtn>
            </ElFormItem>
            <ElFormItem label="会话模板目录">
              <ElInput
                v-model="sessionTemplateDir"
                placeholder="留空用全局 gen.external.template-dir"
                style="width: 320px"
                clearable
              />
            </ElFormItem>
            <ElFormItem>
              <ExternalActionBtn
                what="覆盖会话模板"
                usage="保存自定义 Velocity 模板目录到当前会话，优先于 application.yml 中的 gen.external.template-dir。留空则恢复用全局/内置模板。"
                @click="handleSaveTemplateDir"
              >
                保存模板
              </ExternalActionBtn>
            </ElFormItem>
          </ElForm>
          <ElForm :inline="true" class="mb-4">
            <ElFormItem>
              <div class="action-btn-row">
                <ExternalActionBtn
                  what="写盘前先对比"
                  usage="填写写盘根目录后打开。对比磁盘与即将生成的内容，可勾选文件后确认写入。大项目仅加载摘要，点文件再看 diff。"
                  type="info"
                  v-auth="'tool:external:preview'"
                  :loading="writeDiffLoading"
                  @click="openWriteDiff"
                >
                  写盘 Diff 预览
                </ExternalActionBtn>
                <ExternalActionBtn
                  what="直接写进项目"
                  usage="将代码写入「写盘根目录」下对应路径。勾选「跳过无变化文件」时不会覆盖未改动的文件。需在服务端配置允许路径白名单。"
                  type="warning"
                  v-auth="'tool:external:create'"
                  :loading="writing"
                  @click="handleWriteToDisk"
                >
                  写盘生成
                </ExternalActionBtn>
              </div>
            </ElFormItem>
          </ElForm>
          <ElForm :inline="true" class="mb-4">
            <ElFormItem>
              <ElTooltip
                content="开启后，覆盖已有文件前会先备份到项目下 .star-pivot-gen-backup/ 目录，写盘后可一键回退。"
                placement="top"
              >
                <ElCheckbox v-model="backupBeforeWrite">覆盖前自动备份（可回退）</ElCheckbox>
              </ElTooltip>
            </ElFormItem>
            <ElFormItem>
              <ElTooltip
                content="开启后，「写盘生成」不会覆盖内容与生成结果一致的文件；Diff 确认写盘不受此项影响（按勾选写入）。"
                placement="top"
              >
                <ElCheckbox v-model="writeOnlyChanged">写盘时跳过无变化文件</ElCheckbox>
              </ElTooltip>
            </ElFormItem>
            <ElFormItem v-if="lastWriteBackup">
              <ExternalActionBtn
                what="恢复备份文件"
                usage="将上次写盘备份的原文件写回项目目录。仅恢复曾被覆盖的文件，不会删除本次新增的文件。"
                type="danger"
                :loading="rollingBack"
                v-auth="'tool:external:create'"
                @click="handleRollbackWrite"
              >
                回退上次写盘
              </ExternalActionBtn>
            </ElFormItem>
          </ElForm>
        </template>
        <ElAlert
          v-else
          type="info"
          :closable="false"
          show-icon
          class="mb-4"
          title="当前环境已关闭服务端写盘，请使用「下载 ZIP」将代码保存到本地。"
        />
        <ElForm :inline="true" class="mb-4">
          <ElFormItem>
            <ElTooltip
              content="导入到 gen_table 时，若表名已存在：勾选则覆盖原配置，不勾选则跳过该表。"
              placement="top"
            >
              <ElCheckbox v-model="importOverwrite">覆盖已存在的 gen_table 配置</ElCheckbox>
            </ElTooltip>
          </ElFormItem>
        </ElForm>
        <ElDescriptions :column="2" border size="small">
          <ElDescriptionsItem label="模板"
            >{{ genConfig.tplCategory }} / {{ genConfig.tplWebType }}</ElDescriptionsItem
          >
          <ElDescriptionsItem label="生效模板目录">{{
            effectiveTemplateDir || 'classpath 内置 vm/'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="基础包">{{ pathProfile.basePackage }}</ElDescriptionsItem>
          <ElDescriptionsItem label="API 路径">{{ pathProfile.apiPath }}</ElDescriptionsItem>
          <ElDescriptionsItem label="页面路径">{{ pathProfile.vuePagePath }}</ElDescriptionsItem>
        </ElDescriptions>
      </div>

      <div class="step-footer">
        <ElButton v-if="currentStep > 0" @click="prevStep">上一步</ElButton>
        <ElButton v-if="currentStep < 4" type="primary" :loading="stepLoading" @click="nextStep">
          下一步
        </ElButton>
      </div>
    </ElCard>

    <ExternalPreviewDialog
      v-model:visible="previewVisible"
      :session-id="sessionId"
      :table-name="previewTableName"
      :gen-scope="genScope"
    />
    <ExternalWriteDiffDialog
      v-if="writeToDiskEnabled"
      ref="writeDiffDialogRef"
      v-model:visible="writeDiffVisible"
      :session-id="sessionId"
      :table-names="selectedTableNames"
      :output-root="outputRoot"
      :gen-scope="genScope"
      @confirm-write="handleConfirmWriteFromDiff"
    />
    <ExternalOutputRootPicker
      v-if="writeToDiskEnabled"
      v-model:visible="outputRootPickerVisible"
      @select="onOutputRootPicked"
    />
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import {
    ElAlert,
    ElAutocomplete,
    ElButton,
    ElCard,
    ElCheckbox,
    ElCol,
    ElDescriptions,
    ElDescriptionsItem,
    ElDivider,
    ElForm,
    ElFormItem,
    ElInput,
    ElInputNumber,
    ElMessage,
    ElMessageBox,
    ElOption,
    ElRow,
    ElSelect,
    ElSpace,
    ElStep,
    ElSteps,
    ElTabPane,
    ElTabs,
    ElTag,
    ElTooltip
  } from 'element-plus'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import type { ColumnOption } from '@/types'
  import FileSaver from 'file-saver'
  import { fetchGetDictTypeSelectList, type SysDictType } from '@/api/dict/type'
  import { fetchGetParentMenu } from '@/api/menu/menu'
  import PathConfigForm from './modules/path-config-form.vue'
  import ExternalPreviewDialog from './modules/external-preview-dialog.vue'
  import ExternalWriteDiffDialog from './modules/external-write-diff-dialog.vue'
  import ExternalActionBtn from './modules/external-action-btn.vue'
  import ExternalOutputRootPicker from './modules/external-output-root-picker.vue'
  import ExternalColumnTable from './modules/external-column-table.vue'
  import ExternalTableExtraForm, {
    type ExternalTableMeta
  } from './modules/external-table-extra-form.vue'
  import {
    type ExternalDbConnection,
    type ExternalGenScope,
    type ExternalTableItem,
    type ExternalWriteResult,
    fetchExternalCapabilities,
    fetchExternalColumns,
    fetchExternalConnect,
    fetchExternalDisconnect,
    fetchExternalDownload,
    fetchExternalImportGenTable,
    fetchExternalSaveDraft,
    fetchExternalSaveGenConfig,
    fetchExternalSavePathProfile,
    fetchExternalSaveTemplateDir,
    fetchExternalSessionStatus,
    fetchExternalTables,
    fetchExternalTemplates,
    fetchExternalWriteRollback,
    fetchExternalWriteToDisk,
    type GenPathProfile,
    type GenTableColumnItem
  } from '@/api/generator/gen-external'
  import { loadRecentOutputRoots, rememberOutputRoot } from '@/utils/generator/output-root-presets'
  import {
    clearLastWriteBackup,
    loadLastWriteBackup,
    saveLastWriteBackup
  } from '@/utils/generator/write-backup-state'
  import {
    type ConnectionPreset,
    loadConnectionPresets,
    loadLastConnection,
    persistConnectionPresets,
    saveLastConnection,
    toPresetConnection
  } from '@/utils/generator/external-connection-presets'

  defineOptions({ name: 'GenExternal' })

  const SESSION_KEY = 'gen_external_session_id'

  const currentStep = ref(0)
  const stepLoading = ref(false)
  const sessionId = ref<string>(sessionStorage.getItem(SESSION_KEY) || '')
  const dbInfo = ref('')
  const sessionRemainingSeconds = ref(0)
  let sessionTimer: ReturnType<typeof setInterval> | undefined

  const sessionRemainingText = computed(() => {
    const s = sessionRemainingSeconds.value
    if (s <= 0) return ''
    const m = Math.floor(s / 60)
    const sec = s % 60
    return `剩余 ${m}:${sec.toString().padStart(2, '0')}`
  })

  const connPresets = ref<ConnectionPreset[]>([])
  const selectedConnPreset = ref('')
  const newConnPresetName = ref('')
  const saveConnPassword = ref(false)
  const sessionExpireWarned = ref(false)

  const connection = reactive<ExternalDbConnection>({
    dbType: 'mysql',
    oracleConnectMode: 'service',
    host: '127.0.0.1',
    port: 3306,
    database: '',
    username: 'root',
    password: '',
    params: 'useSSL=false&serverTimezone=Asia/Shanghai'
  })

  const connFormRef = ref<FormInstance>()
  const connRules: FormRules = {
    host: [{ required: true, message: '请输入主机', trigger: 'blur' }],
    port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
    database: [{ required: true, message: '请输入库名', trigger: 'blur' }],
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }

  const tableSearch = reactive({ tableName: '', tableComment: '' })
  const tableLoading = ref(false)
  const tableData = ref<ExternalTableItem[]>([])
  const selectedTables = ref<ExternalTableItem[]>([])
  const tablePagination = reactive({ current: 1, size: 10, total: 0 })

  const tableColumns = ref<ColumnOption[]>([
    { type: 'selection', width: 55 },
    { prop: 'tableName', label: '表名称', minWidth: 160 },
    { prop: 'tableComment', label: '表描述', minWidth: 200 },
    { prop: 'createTime', label: '创建时间', width: 180 }
  ])

  const templateOptions = reactive<{
    categories: { value: string; label: string }[]
    webTypes: { value: string; label: string }[]
  }>({
    categories: [],
    webTypes: []
  })

  const genConfig = reactive({
    tplCategory: 'crud',
    tplWebType: 'art-design-pro',
    author: 'admin'
  })

  const pathProfile = ref<GenPathProfile>({
    basePackage: 'com.star.pivot.system'
  })

  const tableMeta = reactive<Record<string, ExternalTableMeta>>({})
  const tableColumnsMap = reactive<Record<string, GenTableColumnItem[]>>({})
  const dictOptions = ref<SysDictType[]>([])
  const parentMenus = ref<any[]>([])
  const activeTableTab = ref('')
  const previewTableName = ref('')
  const previewVisible = ref(false)
  const downloading = ref(false)
  const importing = ref(false)
  const writing = ref(false)
  const writeDiffLoading = ref(false)
  const writeDiffVisible = ref(false)
  const writeDiffDialogRef = ref<InstanceType<typeof ExternalWriteDiffDialog>>()
  const importOverwrite = ref(false)
  const writeOnlyChanged = ref(true)
  const backupBeforeWrite = ref(true)
  const outputRoot = ref('')
  const outputRootPickerVisible = ref(false)
  const recentOutputRoots = ref<string[]>([])
  const lastWriteBackup = ref<{ backupId: string; outputRoot: string } | null>(null)
  const rollingBack = ref(false)
  const configuredDefaultOutputRoot = ref('')
  const writeToDiskEnabled = ref(true)
  const sessionTemplateDir = ref('')
  const effectiveTemplateDir = ref('')

  const schemaPlaceholder = computed(() => {
    if (connection.dbType === 'postgresql') return 'public'
    if (connection.dbType === 'oracle') return '用户名大写'
    if (connection.dbType === 'sqlserver') return 'dbo'
    return ''
  })

  function dbTypeShortName(dbType?: string) {
    if (dbType === 'postgresql') return 'PG'
    if (dbType === 'oracle') return 'Oracle'
    if (dbType === 'sqlserver') return 'SQL Server'
    return 'MySQL'
  }

  const oracleDatabaseLabel = computed(() => {
    if (connection.dbType !== 'oracle') return '库名'
    if (connection.oracleConnectMode === 'sid') return 'SID'
    if (connection.oracleConnectMode === 'tns') return 'TNS / Service'
    return 'Service 名'
  })

  const oracleDatabasePlaceholder = computed(() => {
    if (connection.dbType !== 'oracle') return 'star_pivot'
    if (connection.oracleConnectMode === 'sid') return 'ORCL'
    if (connection.oracleConnectMode === 'tns') {
      return '(DESCRIPTION=(ADDRESS=...)(CONNECT_DATA=...))'
    }
    return 'XEPDB1 / ORCLPDB'
  })

  const jdbcParamsPlaceholder = computed(() => {
    if (connection.dbType === 'oracle' && connection.oracleConnectMode === 'tns') {
      return '可填完整 jdbc:oracle:thin:@(DESCRIPTION=...) 或留空使用上方 TNS'
    }
    if (connection.dbType === 'sqlserver') return 'encrypt=false;trustServerCertificate=true'
    if (connection.dbType === 'postgresql') return 'sslmode=disable'
    return 'useSSL=false&serverTimezone=Asia/Shanghai'
  })

  const genScope = reactive<ExternalGenScope>({
    genBackend: true,
    genFrontend: true,
    genSql: true
  })

  const selectedTableNames = computed(() => selectedTables.value.map((t) => t.tableName))

  async function loadTemplateOptions() {
    try {
      const res = await fetchExternalTemplates()
      templateOptions.categories = res.categories ?? []
      templateOptions.webTypes = res.webTypes ?? []
    } catch {
      templateOptions.categories = [
        { value: 'crud', label: '单表（增删改查）' },
        { value: 'tree', label: '树表（增删改查）' },
        { value: 'sub', label: '主子表（增删改查）' }
      ]
      templateOptions.webTypes = [
        { value: 'art-design-pro', label: 'Vue3 Art Design Pro' },
        { value: 'element-plus', label: 'Vue3 Element Plus' },
        { value: 'element-ui', label: 'Vue2 Element UI' }
      ]
    }
  }

  async function handleConnect(): Promise<boolean> {
    await connFormRef.value?.validate()
    stepLoading.value = true
    try {
      const res = await fetchExternalConnect(connection)
      sessionId.value = res.sessionId
      sessionStorage.setItem(SESSION_KEY, res.sessionId)
      dbInfo.value = `${res.database} · ${dbTypeShortName(connection.dbType)} ${res.dbVersion}`
      saveLastConnection(connection)
      sessionExpireWarned.value = false
      startSessionTimer()
      await refreshSessionTemplateDir()
      restoreLastWriteBackup()
      ElMessage.success(`连接成功，会话 ${res.expireMinutes} 分钟内有效`)
      return true
    } finally {
      stepLoading.value = false
    }
  }

  function stopSessionTimer() {
    if (sessionTimer) {
      clearInterval(sessionTimer)
      sessionTimer = undefined
    }
    sessionRemainingSeconds.value = 0
  }

  async function refreshSessionStatus() {
    if (!sessionId.value) return
    try {
      const status = await fetchExternalSessionStatus(sessionId.value)
      sessionRemainingSeconds.value = status.remainingSeconds ?? 0
      if (status.effectiveTemplateDir !== undefined) {
        effectiveTemplateDir.value = status.effectiveTemplateDir || ''
      }
      if (status.templateDir !== undefined) {
        sessionTemplateDir.value = status.templateDir || ''
      }
      if (!dbInfo.value && status.database) {
        dbInfo.value = `${status.database}${status.dbVersion ? ` · MySQL ${status.dbVersion}` : ''}`
      }
      if (sessionRemainingSeconds.value <= 0) {
        ElMessage.warning('会话已过期，请重新连接')
        await handleDisconnect()
      } else if (sessionRemainingSeconds.value <= 300 && !sessionExpireWarned.value) {
        sessionExpireWarned.value = true
        ElMessage.warning(`会话即将过期（${sessionRemainingText.value}），请及时完成或重新连接`)
      }
    } catch {
      stopSessionTimer()
    }
  }

  function startSessionTimer() {
    stopSessionTimer()
    refreshSessionStatus()
    sessionTimer = setInterval(refreshSessionStatus, 30_000)
  }

  function loadConnPresetsFromStorage() {
    connPresets.value = loadConnectionPresets()
    const last = loadLastConnection()
    if (last) {
      Object.assign(connection, { ...connection, ...last, password: connection.password })
    }
  }

  function applyConnPreset(name: string) {
    if (!name) return
    const preset = connPresets.value.find((p) => p.name === name)
    if (!preset) return
    Object.assign(connection, preset.connection)
    if (preset.savePassword && preset.password) {
      connection.password = preset.password
      saveConnPassword.value = true
    } else {
      connection.password = ''
      saveConnPassword.value = false
    }
  }

  function saveConnPreset() {
    const name = newConnPresetName.value.trim() || selectedConnPreset.value
    if (!name) {
      ElMessage.warning('请输入预设名称')
      return
    }
    const entry: ConnectionPreset = {
      name,
      connection: toPresetConnection(connection),
      savePassword: saveConnPassword.value,
      password: saveConnPassword.value ? connection.password : undefined
    }
    const idx = connPresets.value.findIndex((p) => p.name === name)
    if (idx >= 0) {
      connPresets.value[idx] = entry
    } else {
      connPresets.value.push(entry)
    }
    persistConnectionPresets(connPresets.value)
    selectedConnPreset.value = name
    newConnPresetName.value = ''
    ElMessage.success('连接预设已保存')
  }

  function deleteConnPreset() {
    const name = selectedConnPreset.value
    if (!name) return
    connPresets.value = connPresets.value.filter((p) => p.name !== name)
    persistConnectionPresets(connPresets.value)
    selectedConnPreset.value = ''
    ElMessage.success('已删除连接预设')
  }

  async function handleDisconnect() {
    if (!sessionId.value) return
    stopSessionTimer()
    try {
      await fetchExternalDisconnect(sessionId.value)
    } catch {
      /* ignore */
    }
    sessionId.value = ''
    sessionStorage.removeItem(SESSION_KEY)
    lastWriteBackup.value = null
    clearLastWriteBackup()
    dbInfo.value = ''
    currentStep.value = 0
    ElMessage.info('已断开连接')
  }

  async function loadTables() {
    if (!sessionId.value) return
    tableLoading.value = true
    try {
      const page = await fetchExternalTables({
        sessionId: sessionId.value,
        pageNum: tablePagination.current,
        pageSize: tablePagination.size,
        tableName: tableSearch.tableName || undefined,
        tableComment: tableSearch.tableComment || undefined
      })
      tableData.value = page.rows ?? []
      tablePagination.total = page.total ?? 0
    } finally {
      tableLoading.value = false
    }
  }

  function resetTableSearch() {
    tableSearch.tableName = ''
    tableSearch.tableComment = ''
    tablePagination.current = 1
    loadTables()
  }

  function onTableSelectionChange(rows: ExternalTableItem[]) {
    selectedTables.value = rows
  }

  function onTableSizeChange(size: number) {
    tablePagination.size = size
    tablePagination.current = 1
    loadTables()
  }

  function onTablePageChange(page: number) {
    tablePagination.current = page
    loadTables()
  }

  function guessBusinessName(tableName: string): string {
    const idx = tableName.lastIndexOf('_')
    return idx >= 0 ? tableName.slice(idx + 1) : tableName
  }

  function guessClassName(tableName: string): string {
    return tableName
      .split('_')
      .map((s) => s.charAt(0).toUpperCase() + s.slice(1).toLowerCase())
      .join('')
  }

  function guessTreeFields(name: string, cols: GenTableColumnItem[]) {
    const meta = tableMeta[name]
    if (!meta || genConfig.tplCategory !== 'tree') return
    if (meta.treeCode && meta.treeParentCode && meta.treeName) return

    const names = cols.map((c) => c.columnName)
    const parentCol =
      names.find((n) => /^(parent_id|pid|parentId)$/i.test(n)) ||
      names.find((n) => n.includes('parent') && n.endsWith('_id'))
    const pkCol = cols.find((c) => c.isPk === '1')?.columnName
    const nameCol =
      names.find((n) => /_name$/.test(n) && !n.includes('user')) ||
      names.find((n) => n === 'name' || n === 'title')

    if (!meta.treeParentCode && parentCol) meta.treeParentCode = parentCol
    if (!meta.treeCode && pkCol) meta.treeCode = pkCol
    if (!meta.treeName && nameCol) meta.treeName = nameCol
  }

  async function loadStep2Resources() {
    const [dictRes, menuRes] = await Promise.all([
      fetchGetDictTypeSelectList(),
      fetchGetParentMenu()
    ])
    const dictData = (dictRes as any)?.data ?? dictRes
    dictOptions.value = Array.isArray(dictData) ? dictData : []
    const menuData = (menuRes as any)?.data ?? menuRes
    parentMenus.value = Array.isArray(menuData) ? menuData : []
  }

  function validateStep2Config(): boolean {
    if (genConfig.tplCategory === 'tree') {
      for (const name of selectedTableNames.value) {
        const meta = tableMeta[name]
        if (!meta?.treeCode || !meta?.treeParentCode || !meta?.treeName) {
          ElMessage.warning(`表 ${name}：请完整配置树编码、树父编码、树名称字段`)
          activeTableTab.value = name
          return false
        }
      }
    }
    if (genConfig.tplCategory === 'sub') {
      for (const name of selectedTableNames.value) {
        const meta = tableMeta[name]
        if (!meta?.subTableName || !meta?.subTableFkName) {
          ElMessage.warning(`表 ${name}：请配置关联子表及外键字段`)
          activeTableTab.value = name
          return false
        }
      }
    }
    return true
  }

  async function loadColumnsForSelected() {
    if (!sessionId.value) return
    for (const row of selectedTables.value) {
      const name = row.tableName
      const cols = await fetchExternalColumns(sessionId.value, name)
      tableColumnsMap[name] = cols
      if (!tableMeta[name]) {
        tableMeta[name] = {
          className: guessClassName(name),
          businessName: guessBusinessName(name),
          functionName: row.tableComment || name,
          tableComment: row.tableComment || name
        }
      }
      guessTreeFields(name, cols)
    }
    activeTableTab.value = selectedTableNames.value[0] ?? ''
    previewTableName.value = selectedTableNames.value[0] ?? ''
  }

  async function saveDrafts() {
    if (!sessionId.value) return
    for (const name of selectedTableNames.value) {
      const meta = tableMeta[name]
      await fetchExternalSaveDraft({
        sessionId: sessionId.value,
        tableName: name,
        tableComment: meta?.tableComment,
        className: meta?.className,
        businessName: meta?.businessName,
        functionName: meta?.functionName,
        treeCode: meta?.treeCode,
        treeParentCode: meta?.treeParentCode,
        treeName: meta?.treeName,
        parentMenuId: meta?.parentMenuId,
        subTableName: meta?.subTableName,
        subTableFkName: meta?.subTableFkName,
        vuePagePath: meta?.vuePagePath,
        apiPath: meta?.apiPath,
        columns: tableColumnsMap[name]
      })
    }
  }

  async function nextStep() {
    if (currentStep.value === 0) {
      const ok = await handleConnect()
      if (ok) {
        currentStep.value = 1
        await loadTables()
      }
      return
    }
    if (currentStep.value === 1) {
      if (selectedTables.value.length === 0) {
        ElMessage.warning('请至少选择一张表')
        return
      }
      stepLoading.value = true
      try {
        await loadColumnsForSelected()
        await loadStep2Resources()
        currentStep.value = 2
      } finally {
        stepLoading.value = false
      }
      return
    }
    if (currentStep.value === 2) {
      if (!genConfig.tplCategory || !genConfig.tplWebType) {
        ElMessage.warning('请选择模板类型')
        return
      }
      if (!validateStep2Config()) return
      stepLoading.value = true
      try {
        await saveDrafts()
        await fetchExternalSaveGenConfig({
          sessionId: sessionId.value,
          tableNames: selectedTableNames.value,
          tplCategory: genConfig.tplCategory,
          tplWebType: genConfig.tplWebType,
          author: genConfig.author,
          pathProfile: pathProfile.value
        })
        if (!pathProfile.value.entityPackage) {
          const base = pathProfile.value.basePackage || 'com.star.pivot.system'
          pathProfile.value.basePackage = base
          pathProfile.value.entityPackage = `${base}.domain.entity`
          pathProfile.value.dtoPackage = `${base}.domain.dto`
          pathProfile.value.voPackage = `${base}.domain.bo`
          pathProfile.value.boPackage = `${base}.domain.bo`
          pathProfile.value.mapperPackage = `${base}.mapper`
          pathProfile.value.servicePackage = `${base}.service`
          pathProfile.value.serviceImplPackage = `${base}.service.impl`
          pathProfile.value.controllerPackage = `${base}.controller`
          const mod = base.split('.').pop() || 'app'
          pathProfile.value.mapperXmlPath = `main/resources/mapper/${mod}`
          pathProfile.value.apiPath = `star-pivot-ui/src/api/${mod}`
          const biz = tableMeta[selectedTableNames.value[0]]?.businessName || 'demo'
          pathProfile.value.vuePagePath = `star-pivot-ui/src/views/${mod}/${biz}`
        }
        currentStep.value = 3
      } finally {
        stepLoading.value = false
      }
      return
    }
    if (currentStep.value === 3) {
      if (!pathProfile.value.basePackage?.trim()) {
        ElMessage.warning('请填写基础包名')
        return
      }
      stepLoading.value = true
      try {
        await fetchExternalSavePathProfile(sessionId.value, pathProfile.value)
        currentStep.value = 4
        previewTableName.value = selectedTableNames.value[0] ?? ''
      } finally {
        stepLoading.value = false
      }
    }
  }

  function prevStep() {
    if (currentStep.value > 0) currentStep.value -= 1
  }

  function validateGenScope(): boolean {
    if (!genScope.genBackend && !genScope.genFrontend && !genScope.genSql) {
      ElMessage.warning('请至少选择一种生成范围')
      return false
    }
    return true
  }

  async function ensureOutputRootForWrite(): Promise<boolean> {
    if (outputRoot.value.trim() || configuredDefaultOutputRoot.value.trim()) {
      return true
    }
    try {
      await ElMessageBox.confirm(
        '未填写写盘根目录，将使用服务端 default-output-root 或 Java 工作目录（user.dir）。是否继续？',
        '写盘根目录',
        { type: 'warning', confirmButtonText: '继续', cancelButtonText: '取消' }
      )
      return true
    } catch {
      return false
    }
  }

  function restoreLastWriteBackup() {
    if (!sessionId.value) {
      lastWriteBackup.value = null
      return
    }
    lastWriteBackup.value = loadLastWriteBackup(sessionId.value)
  }

  function openPreview() {
    if (!previewTableName.value) {
      ElMessage.warning('请选择要预览的表')
      return
    }
    if (!validateGenScope()) return
    previewVisible.value = true
  }

  async function handleDownload() {
    if (!sessionId.value || selectedTableNames.value.length === 0) return
    if (!validateGenScope()) return
    downloading.value = true
    try {
      const blob = await fetchExternalDownload(sessionId.value, selectedTableNames.value, genScope)
      FileSaver.saveAs(blob, `codegen_external_${Date.now()}.zip`)
      ElMessage.success('下载成功')
    } finally {
      downloading.value = false
    }
  }

  function onDbTypeChange(type: string) {
    if (type === 'postgresql') {
      connection.port = 5432
      if (!connection.schema) connection.schema = 'public'
      if (!connection.params) connection.params = ''
    } else if (type === 'oracle') {
      connection.port = 1521
      connection.oracleConnectMode = connection.oracleConnectMode || 'service'
      if (!connection.schema) connection.schema = connection.username?.toUpperCase() || ''
      if (!connection.params) connection.params = ''
    } else if (type === 'sqlserver') {
      connection.port = 1433
      if (!connection.schema) connection.schema = 'dbo'
      if (!connection.params) connection.params = 'encrypt=false;trustServerCertificate=true'
    } else {
      connection.port = 3306
      if (!connection.params) {
        connection.params = 'useSSL=false&serverTimezone=Asia/Shanghai'
      }
    }
  }

  async function syncPathProfileToServer() {
    if (!sessionId.value || !pathProfile.value.basePackage?.trim()) return
    await fetchExternalSavePathProfile(sessionId.value, pathProfile.value)
  }

  async function openWriteDiff() {
    if (!sessionId.value || selectedTableNames.value.length === 0) return
    if (!validateGenScope()) return
    if (!(await ensureOutputRootForWrite())) return
    writeDiffLoading.value = true
    try {
      await syncPathProfileToServer()
      writeDiffVisible.value = true
    } finally {
      writeDiffLoading.value = false
    }
  }

  async function handleSaveTemplateDir() {
    if (!sessionId.value) return
    await fetchExternalSaveTemplateDir(sessionId.value, sessionTemplateDir.value.trim())
    await refreshSessionTemplateDir()
    ElMessage.success('会话模板目录已保存')
  }

  async function refreshSessionTemplateDir() {
    if (!sessionId.value) return
    const status = await fetchExternalSessionStatus(sessionId.value)
    effectiveTemplateDir.value = status.effectiveTemplateDir || ''
    sessionTemplateDir.value = status.templateDir || ''
  }

  function queryOutputRoots(queryString: string, cb: (items: { value: string }[]) => void) {
    const q = queryString.trim().toLowerCase()
    const list = recentOutputRoots.value
      .filter((p) => !q || p.toLowerCase().includes(q))
      .map((p) => ({ value: p }))
    cb(list)
  }

  function onOutputRootPicked(path: string) {
    outputRoot.value = path
  }

  function applyWriteResult(res: ExternalWriteResult) {
    rememberOutputRoot(res.outputRoot)
    recentOutputRoots.value = loadRecentOutputRoots()
    if (res.backupId && (res.backedUpCount ?? 0) > 0 && sessionId.value) {
      lastWriteBackup.value = { backupId: res.backupId, outputRoot: res.outputRoot }
      saveLastWriteBackup({
        sessionId: sessionId.value,
        backupId: res.backupId,
        outputRoot: res.outputRoot
      })
    }
    const backupTip =
      res.backedUpCount && res.backedUpCount > 0 ? `，已备份 ${res.backedUpCount} 个原文件` : ''
    ElMessage.success(`已写入 ${res.fileCount} 个文件到 ${res.outputRoot}${backupTip}`)
  }

  async function handleRollbackWrite() {
    if (!sessionId.value || !lastWriteBackup.value) return
    await ElMessageBox.confirm(
      '将用备份覆盖恢复上次写盘改动的文件，是否继续？（不会删除本次新增的文件）',
      '回退写盘',
      { type: 'warning' }
    )
    rollingBack.value = true
    try {
      const res = await fetchExternalWriteRollback(
        sessionId.value,
        lastWriteBackup.value.backupId,
        lastWriteBackup.value.outputRoot
      )
      ElMessage.success(`已恢复 ${res.fileCount} 个文件`)
      lastWriteBackup.value = null
      clearLastWriteBackup()
    } finally {
      rollingBack.value = false
    }
  }

  async function handleConfirmWriteFromDiff(selectedPaths: string[]) {
    if (!sessionId.value || selectedTableNames.value.length === 0) return
    if (!selectedPaths.length) return
    writeDiffDialogRef.value?.setWriting(true)
    try {
      await syncPathProfileToServer()
      const res = await fetchExternalWriteToDisk(
        sessionId.value,
        selectedTableNames.value,
        outputRoot.value.trim() || undefined,
        genScope,
        { selectedPaths, backupBeforeWrite: backupBeforeWrite.value }
      )
      applyWriteResult(res)
      writeDiffVisible.value = false
    } finally {
      writeDiffDialogRef.value?.setWriting(false)
    }
  }

  async function handleWriteToDisk() {
    if (!sessionId.value || selectedTableNames.value.length === 0) return
    if (!validateGenScope()) return
    if (!(await ensureOutputRootForWrite())) return
    writing.value = true
    try {
      await syncPathProfileToServer()
      const res = await fetchExternalWriteToDisk(
        sessionId.value,
        selectedTableNames.value,
        outputRoot.value.trim() || undefined,
        genScope,
        { onlyChanged: writeOnlyChanged.value, backupBeforeWrite: backupBeforeWrite.value }
      )
      applyWriteResult(res)
    } finally {
      writing.value = false
    }
  }

  async function handleImportGenTable() {
    if (!sessionId.value || selectedTableNames.value.length === 0) return
    importing.value = true
    try {
      const res = await fetchExternalImportGenTable(
        sessionId.value,
        selectedTableNames.value,
        importOverwrite.value
      )
      const parts: string[] = []
      if (res.imported?.length) parts.push(`新增 ${res.imported.length} 张`)
      if (res.updated?.length) parts.push(`覆盖 ${res.updated.length} 张`)
      if (res.skipped?.length) parts.push(`跳过 ${res.skipped.length} 张`)
      ElMessage.success(parts.length ? parts.join('，') : '导入完成')
    } finally {
      importing.value = false
    }
  }

  watch(
    () => genConfig.tplCategory,
    (cat) => {
      if (cat !== 'tree') return
      for (const name of selectedTableNames.value) {
        guessTreeFields(name, tableColumnsMap[name] || [])
      }
    }
  )

  onMounted(() => {
    loadTemplateOptions()
    loadConnPresetsFromStorage()
    recentOutputRoots.value = loadRecentOutputRoots()
    if (recentOutputRoots.value.length && !outputRoot.value) {
      outputRoot.value = recentOutputRoots.value[0]
    }
    fetchExternalCapabilities()
      .then((cap) => {
        writeToDiskEnabled.value = cap.writeToDiskEnabled !== false
        if (cap.backupBeforeWrite !== undefined) {
          backupBeforeWrite.value = cap.backupBeforeWrite
        }
        configuredDefaultOutputRoot.value = cap.defaultOutputRoot || ''
        if (cap.defaultOutputRoot && !outputRoot.value) {
          outputRoot.value = cap.defaultOutputRoot
        }
      })
      .catch(() => {})
    restoreLastWriteBackup()
    if (sessionId.value) {
      ElMessageBox.confirm('检测到未断开的生成会话，是否继续使用？', '提示', {
        confirmButtonText: '继续',
        cancelButtonText: '重新连接',
        type: 'info'
      })
        .then(() => {
          currentStep.value = 1
          startSessionTimer()
          loadTables()
        })
        .catch(() => {
          handleDisconnect()
        })
    }
  })

  onBeforeUnmount(() => {
    stopSessionTimer()
  })
</script>

<style scoped lang="scss">
  .gen-external-page {
    padding: 12px;

    .step-card {
      min-height: calc(100vh - 120px);
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .title {
        font-size: 16px;
        font-weight: 600;
      }
    }

    .step-panel {
      min-height: 360px;
      padding: 8px 0 24px;
    }

    .conn-form {
      max-width: 960px;
      margin: 0 auto;
    }

    .conn-preset-bar {
      max-width: 960px;
      margin: 0 auto;
    }

    .session-ttl {
      opacity: 0.85;
    }

    .step-footer {
      display: flex;
      gap: 12px;
      justify-content: center;
      padding-top: 16px;
      border-top: 1px solid var(--el-border-color-lighter);
    }

    .action-btn-row {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      align-items: center;
    }

    .w-full {
      width: 100%;
    }
  }
</style>
