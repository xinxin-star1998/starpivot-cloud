<!-- 代码生成配置编辑页面 -->
<template>
  <div class="generator-edit-page art-full-height">
    <ElTabs v-model="activeTab">
      <!-- 基本信息 -->
      <ElTabPane label="基本信息" name="base">
        <ElCard shadow="never">
          <ElForm :model="baseForm" label-width="120px">
            <ElFormItem label="表名称">
              <ElInput v-model="baseForm.tableName" disabled />
            </ElFormItem>
            <ElFormItem label="表描述">
              <ElInput v-model="baseForm.tableComment" />
            </ElFormItem>
            <ElFormItem label="实体类名称">
              <ElInput v-model="baseForm.className" />
            </ElFormItem>
            <ElFormItem label="作者">
              <ElInput v-model="baseForm.functionAuthor" />
            </ElFormItem>
            <ElFormItem label="备注">
              <ElInput v-model="baseForm.remark" type="textarea" :rows="3" />
            </ElFormItem>
          </ElForm>
        </ElCard>
      </ElTabPane>

      <!-- 字段信息 -->
      <ElTabPane label="字段信息" name="field">
        <ElCard shadow="never">
          <ElTable
            :data="columnList"
            row-key="columnId"
            border
            style="width: 100%"
            :max-height="tableMaxHeight"
          >
            <ElTableColumn type="index" label="序号" width="60" class-name="allowDrag" />
            <ElTableColumn
              prop="columnName"
              label="字段列名"
              min-width="120"
              :show-overflow-tooltip="true"
              class-name="allowDrag"
            />
            <ElTableColumn label="字段描述" min-width="120">
              <template #default="{ row }">
                <ElInput v-model="row.columnComment" />
              </template>
            </ElTableColumn>
            <ElTableColumn
              prop="columnType"
              label="物理类型"
              min-width="120"
              :show-overflow-tooltip="true"
            />
            <ElTableColumn label="Java类型" min-width="130">
              <template #default="{ row }">
                <ElSelect v-model="row.javaType">
                  <ElOption label="Long" value="Long" />
                  <ElOption label="String" value="String" />
                  <ElOption label="Integer" value="Integer" />
                  <ElOption label="Double" value="Double" />
                  <ElOption label="BigDecimal" value="BigDecimal" />
                  <ElOption label="Date" value="Date" />
                  <ElOption label="Boolean" value="Boolean" />
                </ElSelect>
              </template>
            </ElTableColumn>
            <ElTableColumn label="java属性" min-width="120">
              <template #default="{ row }">
                <ElInput v-model="row.javaField" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="插入" min-width="40">
              <template #default="{ row }">
                <ElCheckbox true-value="1" false-value="0" v-model="row.isInsert" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="编辑" min-width="40">
              <template #default="{ row }">
                <ElCheckbox true-value="1" false-value="0" v-model="row.isEdit" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="列表" min-width="40">
              <template #default="{ row }">
                <ElCheckbox true-value="1" false-value="0" v-model="row.isList" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="查询" min-width="40">
              <template #default="{ row }">
                <ElCheckbox true-value="1" false-value="0" v-model="row.isQuery" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="查询方式" min-width="130">
              <template #default="{ row }">
                <ElSelect v-model="row.queryType">
                  <ElOption label="=" value="EQ" />
                  <ElOption label="!=" value="NE" />
                  <ElOption label=">" value="GT" />
                  <ElOption label=">=" value="GTE" />
                  <ElOption label="<" value="LT" />
                  <ElOption label="<=" value="LTE" />
                  <ElOption label="LIKE" value="LIKE" />
                  <ElOption label="BETWEEN" value="BETWEEN" />
                </ElSelect>
              </template>
            </ElTableColumn>
            <ElTableColumn label="必填" min-width="50">
              <template #default="{ row }">
                <ElCheckbox true-value="1" false-value="0" v-model="row.isRequired" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="显示类型" min-width="140">
              <template #default="{ row }">
                <ElSelect v-model="row.htmlType">
                  <ElOption label="文本框" value="input" />
                  <ElOption label="文本域" value="textarea" />
                  <ElOption label="下拉框" value="select" />
                  <ElOption label="单选框" value="radio" />
                  <ElOption label="复选框" value="checkbox" />
                  <ElOption label="日期控件" value="datetime" />
                  <ElOption label="图片上传" value="imageUpload" />
                  <ElOption label="文件上传" value="fileUpload" />
                  <ElOption label="富文本控件" value="editor" />
                </ElSelect>
              </template>
            </ElTableColumn>
            <ElTableColumn label="字典类型" min-width="180">
              <template #default="{ row }">
                <ElSelect v-model="row.dictType" clearable filterable placeholder="请选择">
                  <ElOption
                    v-for="dict in dictOptions"
                    :key="dict.dictType"
                    :label="dict.dictName"
                    :value="dict.dictType"
                  >
                    <span style="float: left">{{ dict?.dictName }}</span>
                    <span style="float: right; font-size: 13px; color: #8492a6">
                      {{ dict?.dictType }}
                    </span>
                  </ElOption>
                </ElSelect>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </ElTabPane>

      <!-- 生成信息 -->
      <ElTabPane label="生成信息" name="gen">
        <ElCard shadow="never">
          <GenInfoForm
            v-model="genInfoForm"
            :tables="tables"
            :columns="columnList"
            :menus="parentMenus"
          />
        </ElCard>
      </ElTabPane>
    </ElTabs>

    <!-- 统一底部操作栏 -->
    <div class="generator-edit-footer">
      <ElButton @click="handleBack">返回</ElButton>
      <ElButton type="primary" @click="handleSubmit">提交</ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * 代码生成配置编辑逻辑
   * 从后端接口获取表的基本信息、字段信息、生成信息
   */
  import { useRoute, useRouter } from 'vue-router'
  import {
    ElButton,
    ElCard,
    ElCheckbox,
    ElForm,
    ElFormItem,
    ElInput,
    ElMessage,
    ElOption,
    ElSelect,
    ElTable,
    ElTableColumn,
    ElTabPane,
    ElTabs
  } from 'element-plus'
  import { fetchEditSave, fetchGetGenTableInfo } from '@/api/generator/gen-table'
  import { fetchGetDictTypeSelectList, type SysDictType } from '@/api/dict/type'
  import { fetchGetParentMenu } from '@/api/menu/menu'

  const GenInfoForm = defineAsyncComponent(
    () => import('@views/tools/generator/modules/genInfoForm.vue')
  )

  // 当前标签页
  const activeTab = ref<'base' | 'field' | 'gen'>('base')

  const route = useRoute()
  const router = useRouter()

  // 基本信息表单数据
  const baseForm = reactive({
    // 表主键ID（编辑保存时必须携带，后端才能根据ID更新记录）
    tableId: 0,
    tableName: '',
    tableComment: '',
    className: '',
    functionAuthor: '',
    remark: ''
  })

  // 生成信息表单数据
  const genInfoForm = reactive({
    tplCategory: 'crud',
    tplWebType: 'element-ui',
    packageName: '',
    moduleName: '',
    businessName: '',
    functionName: '',
    treeCode: '',
    treeParentCode: '',
    treeName: '',
    parentMenuId: undefined as number | string | undefined,
    parentMenuName: ''
  })

  // 字段列表数据
  const columnList = ref<any[]>([])
  // 字段表格最大高度（用于固定表头）
  const tableMaxHeight = ref(480)

  // 所有表信息（生成信息中可能会用到）
  const tables = ref<any[]>([])

  // 字典选项（后续可从字典接口加载）
  const dictOptions = ref<SysDictType[]>([])
  // 上级菜单树
  const parentMenus = ref<any[]>([])

  const loading = ref(false)

  /**
   * 加载代码生成配置信息
   */
  const loadData = async () => {
    const tableId = Number(route.params.tableId)
    if (!tableId) {
      ElMessage.error('缺少表ID参数')
      router.back()
      return
    }
    loading.value = true
    try {
      const [genRes, dictRes, menuRes] = await Promise.all([
        fetchGetGenTableInfo(tableId),
        fetchGetDictTypeSelectList(),
        fetchGetParentMenu()
      ])
      const data = (genRes as any)?.data ?? genRes

      const info = data.info || {}
      // 记录表主键ID，提交时用于后端更新
      baseForm.tableId = info.tableId || 0
      baseForm.tableName = info.tableName || ''
      baseForm.tableComment = info.tableComment || ''
      baseForm.className = info.className || ''
      baseForm.functionAuthor = info.functionAuthor || ''
      baseForm.remark = info.remark || ''

      // 填充生成信息表单
      genInfoForm.tplCategory = info.tplCategory || 'crud'
      genInfoForm.tplWebType = info.tplWebType || 'element-ui'
      genInfoForm.packageName = info.packageName || ''
      genInfoForm.moduleName = info.moduleName || ''
      genInfoForm.businessName = info.businessName || ''
      genInfoForm.functionName = info.functionName || ''
      // 树表与上级菜单等生成附加信息（用于编辑回显）
      // 这些字段在后端从 options 中解析后，已经设置回 GenTable 对象
      genInfoForm.treeCode = info.treeCode || ''
      genInfoForm.treeParentCode = info.treeParentCode || ''
      genInfoForm.treeName = info.treeName || ''
      genInfoForm.parentMenuId = info.parentMenuId ?? undefined
      genInfoForm.parentMenuName = info.parentMenuName || ''

      columnList.value = data.rows || []
      tables.value = data.tables || []

      const dictData = (dictRes as any)?.data ?? dictRes
      dictOptions.value = Array.isArray(dictData) ? dictData : []

      const menuData = (menuRes as any)?.data ?? menuRes
      parentMenus.value = Array.isArray(menuData) ? menuData : []
    } catch (error) {
      if (import.meta.env.DEV) {
        console.error('获取代码生成信息失败：', error)
      }
      ElMessage.error('获取代码生成信息失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 返回上一页
   */
  const handleBack = () => {
    router.back()
  }

  const handleSubmit = async () => {
    // 组合基础信息 + 生成信息 + 字段信息，组装为 genTable 对象
    const genTable: any = {
      ...baseForm, // 包含表的基础信息（tableId, tableName, tableComment, className, functionAuthor, remark）
      ...genInfoForm,
      genType: '0',
      columns: columnList.value // 当前页面维护的字段列表
    }

    // 从 genTable 中拆出树表和菜单相关字段，集中放入 params 中
    const { treeCode, treeName, treeParentCode, parentMenuId } = genTable

    genTable.params = {
      ...(genTable.params || {}),
      treeCode,
      treeName,
      treeParentCode,
      parentMenuId
    }

    // 顶层不再保留这几个字段，避免与后端实体重复
    delete genTable.treeCode
    delete genTable.treeName
    delete genTable.treeParentCode
    delete genTable.parentMenuId

    try {
      await fetchEditSave(genTable)
      ElMessage.success('修改成功')
      // 修改成功后返回代码生成列表页
      router.push('/tools/generator')
    } catch (error) {
      // 错误消息已在 HTTP 拦截器中统一处理并显示
      if (import.meta.env.DEV) {
        console.error('保存代码生成配置失败：', error)
      }
    }
  }
  onMounted(() => {
    loadData()
  })
</script>

<style scoped lang="scss">
  .generator-edit-page {
    padding: 16px;
    background-color: var(--default-bg-color);
  }

  :deep(.el-tabs) {
    .el-tabs__header {
      margin-bottom: 16px;
    }

    .el-tabs__item {
      font-weight: 500;
      transition: all 0.3s ease;

      &.is-active {
        color: var(--el-color-primary);
      }

      &:hover {
        color: var(--el-color-primary-light-3);
      }
    }
  }

  :deep(.el-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: 0 2px 12px 0 rgb(0 0 0 / 8%);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px 0 rgb(0 0 0 / 12%);
    }

    .el-card__body {
      padding: 24px;
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--art-gray-700);
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 2px 8px 0 rgb(0 0 0 / 8%);
    }
  }

  :deep(.el-table) {
    border-radius: 8px;

    .el-table__header-wrapper {
      th {
        font-weight: 600;
        color: var(--art-gray-800);
        background-color: var(--art-gray-100) !important;
      }
    }

    .el-table__body-wrapper {
      tr {
        transition: all 0.2s ease;

        &:hover > td {
          background-color: var(--art-gray-50) !important;
        }
      }
    }
  }

  :deep(.el-select) {
    width: 100%;

    .el-select__wrapper {
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 2px 8px 0 rgb(0 0 0 / 8%);
      }
    }
  }

  .generator-edit-footer {
    padding: 16px;
    margin-top: 24px;
    text-align: center;
    background-color: var(--art-gray-50);
    border-radius: 12px;

    .el-button {
      min-width: 100px;
      font-weight: 500;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-1px);
      }
    }
  }
</style>
