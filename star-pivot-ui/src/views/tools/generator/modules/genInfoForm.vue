<!-- 生成信息表单子组件（Vue3 + Element Plus 重构版） -->
<template>
  <ElForm ref="genInfoFormRef" :model="form" :rules="rules" label-width="150px">
    <ElRow :gutter="16">
      <ElCol :span="12">
        <ElFormItem prop="tplCategory" label="生成模板">
          <ElSelect v-model="form.tplCategory" @change="onTplCategoryChange">
            <ElOption label="单表（增删改查）" value="crud" />
            <ElOption label="树表（增删改查）" value="tree" />
            <ElOption label="主子表（增删改查）" value="sub" />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem prop="tplWebType" label="前端类型">
          <ElSelect v-model="form.tplWebType">
            <ElOption label="Vue2 Element UI 模版" value="element-ui" />
            <ElOption label="Vue3 Art Design Pro 模版" value="art-design-pro" />
            <ElOption label="Vue3 Element Plus 模版" value="element-plus" />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem prop="packageName">
          <template #label>
            生成包路径
            <ElTooltip content="生成在哪个 Java 包下，例如 com.star.pivot.system" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElInput v-model="form.packageName" />
        </ElFormItem>
      </ElCol>

      <ElCol :span="12">
        <ElFormItem prop="moduleName">
          <template #label>
            生成模块名
            <ElTooltip content="可理解为子系统名，例如 system" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElInput v-model="form.moduleName" />
        </ElFormItem>
      </ElCol>

      <ElCol :span="12">
        <ElFormItem prop="businessName">
          <template #label>
            生成业务名
            <ElTooltip content="可理解为功能英文名，例如 user" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElInput v-model="form.businessName" />
        </ElFormItem>
      </ElCol>

      <ElCol :span="12">
        <ElFormItem prop="functionName">
          <template #label>
            生成功能名
            <ElTooltip content="用作类描述，例如 用户" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElInput v-model="form.functionName" />
        </ElFormItem>
      </ElCol>

      <ElCol :span="12">
        <ElFormItem>
          <template #label>
            上级菜单
            <ElTooltip content="分配到指定菜单下，例如 系统管理" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElTreeSelect
            v-model="form.parentMenuId"
            :data="menuTreeOptions"
            check-strictly
            :render-after-expand="false"
            placeholder="请选择系统菜单"
          />
        </ElFormItem>
      </ElCol>
    </ElRow>

    <!-- 树模板额外配置 -->
    <ElRow v-show="form.tplCategory === 'tree'">
      <ElCol :span="24">
        <h4 class="form-header">其他信息</h4>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem>
          <template #label>
            树编码字段
            <ElTooltip content="树显示的编码字段名，如：dept_id" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElSelect v-model="form.treeCode" placeholder="请选择">
            <ElOption
              v-for="(column, index) in columns"
              :key="index"
              :label="`${column.columnName}：${column.columnComment}`"
              :value="column.columnName"
            />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem>
          <template #label>
            树父编码字段
            <ElTooltip content="树显示的父编码字段名，如：parent_id" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElSelect v-model="form.treeParentCode" placeholder="请选择">
            <ElOption
              v-for="(column, index) in columns"
              :key="index"
              :label="`${column.columnName}：${column.columnComment}`"
              :value="column.columnName"
            />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem>
          <template #label>
            树名称字段
            <ElTooltip content="树节点的显示名称字段名，如：dept_name" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElSelect v-model="form.treeName" placeholder="请选择">
            <ElOption
              v-for="(column, index) in columns"
              :key="index"
              :label="`${column.columnName}：${column.columnComment}`"
              :value="column.columnName"
            />
          </ElSelect>
        </ElFormItem>
      </ElCol>
    </ElRow>

    <!-- 主子表关联配置 -->
    <ElRow v-show="form.tplCategory === 'sub'">
      <ElCol :span="24">
        <h4 class="form-header">关联信息</h4>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem>
          <template #label>
            关联子表的表名
            <ElTooltip content="关联子表的表名，如：sys_user" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElSelect v-model="form.subTableName" placeholder="请选择" @change="onSubTableChange">
            <ElOption
              v-for="(table, index) in tables"
              :key="index"
              :label="`${table.tableName}：${table.tableComment}`"
              :value="table.tableName"
            />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :span="12">
        <ElFormItem>
          <template #label>
            子表关联的外键名
            <ElTooltip content="子表关联的外键名，如：user_id" placement="top">
              <ArtSvgIcon icon="ri:question-line" class="ml-1" />
            </ElTooltip>
          </template>
          <ElSelect v-model="form.subTableFkName" placeholder="请选择">
            <ElOption
              v-for="(column, index) in subColumns"
              :key="index"
              :label="`${column.columnName}：${column.columnComment}`"
              :value="column.columnName"
            />
          </ElSelect>
        </ElFormItem>
      </ElCol>
    </ElRow>
  </ElForm>
</template>

<script setup lang="ts">
  /**
   * 生成信息表单子组件（Vue3 + Element Plus）
   * 使用 v-model 绑定父组件中的生成配置对象，并支持树表 / 主子表等高级配置
   */
  import type { FormInstance, FormRules } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  interface GenInfoFormModel {
    tplCategory?: string
    tplWebType?: string
    packageName?: string
    moduleName?: string
    businessName?: string
    functionName?: string
    // 树表相关
    treeCode?: string
    treeParentCode?: string
    treeName?: string
    // 主子表相关
    subTableName?: string
    subTableFkName?: string
    // 上级菜单
    parentMenuId?: number | string
    parentMenuName?: string
  }

  interface ColumnItem {
    columnName: string
    columnComment?: string
  }

  interface TableItem {
    tableName: string
    tableComment?: string
    columns?: ColumnItem[]
  }

  interface MenuItem {
    menuId: number | string
    menuName: string
    children?: MenuItem[]
  }

  interface Props {
    modelValue: GenInfoFormModel
    tables?: TableItem[]
    columns?: ColumnItem[]
    menus?: MenuItem[]
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{
    (e: 'update:modelValue', value: GenInfoFormModel): void
  }>()

  // 与父组件双向绑定的表单数据
  const form = computed<GenInfoFormModel>({
    get: () => props.modelValue,
    set: (val: GenInfoFormModel) => emit('update:modelValue', val)
  })

  const genInfoFormRef = ref<FormInstance>()

  // 子表字段集合
  const subColumns = ref<ColumnItem[]>([])

  // 菜单树选项转换
  const menuTreeOptions = computed(() => {
    const toTree = (node: MenuItem): any => ({
      value: node.menuId,
      label: node.menuName,
      children: node.children ? node.children.map(toTree) : undefined
    })
    return (props.menus || []).map(toTree)
  })

  // 表单校验规则
  const rules = computed<FormRules>(() => ({
    tplCategory: [{ required: true, message: '请选择生成模板', trigger: 'change' }],
    packageName: [{ required: true, message: '请输入生成包路径', trigger: 'blur' }],
    moduleName: [{ required: true, message: '请输入生成模块名', trigger: 'blur' }],
    businessName: [{ required: true, message: '请输入生成业务名', trigger: 'blur' }],
    functionName: [{ required: true, message: '请输入生成功能名', trigger: 'blur' }]
  }))

  /**
   * 选择生成模板触发
   */
  const onTplCategoryChange = (value: string) => {
    if (value !== 'sub') {
      form.value.subTableName = ''
      form.value.subTableFkName = ''
      subColumns.value = []
    }
  }

  /**
   * 选择子表名时重置外键并加载子表字段
   */
  const onSubTableChange = (value: string) => {
    form.value.subTableFkName = ''
    setSubTableColumns(value)
  }

  /**
   * 根据当前选中子表设置子表字段集合
   */
  const setSubTableColumns = (tableName?: string) => {
    const table = (props.tables || []).find((item: TableItem) => item.tableName === tableName)
    subColumns.value = table?.columns || []
  }

  // 监听子表名变化，保持子表字段集合同步
  watch(
    () => form.value.subTableName,
    (val: GenInfoFormModel['subTableName']) => {
      setSubTableColumns(val)
    }
  )

  // 监听前端模板为空时设置默认值
  watch(
    () => form.value.tplWebType,
    (val: GenInfoFormModel['tplWebType']) => {
      if (!val) {
        form.value.tplWebType = 'element-ui'
      }
    }
  )
</script>

<style scoped lang="scss">
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--art-gray-700);
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 2px 8px 0 rgb(0 0 0 / 8%);
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

  :deep(.el-radio-group) {
    .el-radio {
      margin-right: 20px;
    }
  }

  :deep(.el-tree-select) {
    width: 100%;

    .el-select__wrapper {
      border-radius: 8px;
    }
  }

  .form-header {
    padding-bottom: 12px;
    margin-bottom: 16px;
    font-size: 16px;
    font-weight: 600;
    color: var(--art-gray-800);
    border-bottom: 1px solid var(--art-card-border);
  }
</style>
