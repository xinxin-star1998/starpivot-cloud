<template>
  <ElForm :model="meta" label-width="120px" class="external-table-extra">
    <ElRow :gutter="16">
      <ElCol :span="8">
        <ElFormItem label="实体类名">
          <ElInput v-model="meta.className" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="8">
        <ElFormItem label="业务名">
          <ElInput v-model="meta.businessName" placeholder="用于路径、权限" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="8">
        <ElFormItem label="功能名">
          <ElInput v-model="meta.functionName" placeholder="中文描述" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="8">
        <ElFormItem label="表描述">
          <ElInput v-model="meta.tableComment" />
        </ElFormItem>
      </ElCol>
      <ElCol :span="8">
        <ElFormItem label="上级菜单">
          <ElTreeSelect
            v-model="meta.parentMenuId"
            :data="menuTreeOptions"
            check-strictly
            :render-after-expand="false"
            clearable
            placeholder="菜单 SQL 挂载位置"
            class="w-full"
          />
        </ElFormItem>
      </ElCol>
      <ElCol :span="8">
        <ElFormItem label="页面路径">
          <ElInput v-model="meta.vuePagePath" placeholder="留空使用全局配置" clearable />
        </ElFormItem>
      </ElCol>
      <ElCol :span="8">
        <ElFormItem label="API 路径">
          <ElInput v-model="meta.apiPath" placeholder="留空使用全局配置" clearable />
        </ElFormItem>
      </ElCol>
    </ElRow>

    <template v-if="tplCategory === 'tree'">
      <ElDivider content-position="left">树表配置</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="8">
          <ElFormItem label="树编码字段">
            <ElSelect v-model="meta.treeCode" placeholder="请选择" clearable>
              <ElOption
                v-for="col in columns"
                :key="col.columnName"
                :label="`${col.columnName}：${col.columnComment || ''}`"
                :value="col.columnName"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="8">
          <ElFormItem label="树父编码字段">
            <ElSelect v-model="meta.treeParentCode" placeholder="请选择" clearable>
              <ElOption
                v-for="col in columns"
                :key="col.columnName"
                :label="`${col.columnName}：${col.columnComment || ''}`"
                :value="col.columnName"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="8">
          <ElFormItem label="树名称字段">
            <ElSelect v-model="meta.treeName" placeholder="请选择" clearable>
              <ElOption
                v-for="col in columns"
                :key="col.columnName"
                :label="`${col.columnName}：${col.columnComment || ''}`"
                :value="col.columnName"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>
    </template>

    <template v-if="tplCategory === 'sub'">
      <ElDivider content-position="left">主子表配置</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="关联子表">
            <ElSelect
              v-model="meta.subTableName"
              placeholder="从已选表中选择"
              clearable
              @change="onSubTableChange"
            >
              <ElOption
                v-for="name in subTableCandidates"
                :key="name"
                :label="name"
                :value="name"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="子表外键">
            <ElSelect v-model="meta.subTableFkName" placeholder="请选择" clearable>
              <ElOption
                v-for="col in subColumns"
                :key="col.columnName"
                :label="`${col.columnName}：${col.columnComment || ''}`"
                :value="col.columnName"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>
    </template>
  </ElForm>
</template>

<script setup lang="ts">
import {ElCol, ElDivider, ElForm, ElFormItem, ElInput, ElOption, ElRow, ElSelect, ElTreeSelect} from 'element-plus'
import type {GenTableColumnItem} from '@/api/generator/gen-external'

export interface ExternalTableMeta {
    className: string
    businessName: string
    functionName: string
    tableComment?: string
    parentMenuId?: number | string
    treeCode?: string
    treeParentCode?: string
    treeName?: string
    subTableName?: string
    subTableFkName?: string
    /** 表级页面路径覆盖（留空用全局 pathProfile） */
    vuePagePath?: string
    /** 表级 API 路径覆盖 */
    apiPath?: string
  }

  interface MenuItem {
    menuId: number | string
    menuName: string
    children?: MenuItem[]
  }

  const props = defineProps<{
    meta: ExternalTableMeta
    tplCategory: string
    columns: GenTableColumnItem[]
    currentTableName: string
    selectedTableNames: string[]
    parentMenus: MenuItem[]
    allColumnsMap: Record<string, GenTableColumnItem[]>
  }>()

  const subColumns = ref<GenTableColumnItem[]>([])

  const subTableCandidates = computed(() =>
    props.selectedTableNames.filter((name) => name !== props.currentTableName)
  )

  const menuTreeOptions = computed(() => {
    const toTree = (node: MenuItem) => ({
      value: node.menuId,
      label: node.menuName,
      children: node.children?.map(toTree)
    })
    return props.parentMenus.map(toTree)
  })

  function onSubTableChange(name?: string) {
    props.meta.subTableFkName = ''
    subColumns.value = name ? props.allColumnsMap[name] || [] : []
  }

  watch(
    () => props.meta.subTableName,
    (name) => {
      subColumns.value = name ? props.allColumnsMap[name] || [] : []
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">
  .external-table-extra {
    .w-full {
      width: 100%;
    }
  }
</style>
