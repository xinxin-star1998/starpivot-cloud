<template>
  <ElTable :data="columns" border size="small" :max-height="maxHeight" style="width: 100%">
    <ElTableColumn type="index" label="#" width="50" />
    <ElTableColumn prop="columnName" label="字段列名" width="130" show-overflow-tooltip />
    <ElTableColumn label="字段描述" min-width="120">
      <template #default="{ row }">
        <ElInput v-model="row.columnComment" size="small" />
      </template>
    </ElTableColumn>
    <ElTableColumn prop="columnType" label="物理类型" width="110" show-overflow-tooltip />
    <ElTableColumn label="Java类型" width="120">
      <template #default="{ row }">
        <ElSelect v-model="row.javaType" size="small">
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
    <ElTableColumn label="java属性" width="120">
      <template #default="{ row }">
        <ElInput v-model="row.javaField" size="small" />
      </template>
    </ElTableColumn>
    <ElTableColumn label="插入" width="52" align="center">
      <template #default="{ row }">
        <ElCheckbox v-model="row.isInsert" true-value="1" false-value="0" />
      </template>
    </ElTableColumn>
    <ElTableColumn label="编辑" width="52" align="center">
      <template #default="{ row }">
        <ElCheckbox v-model="row.isEdit" true-value="1" false-value="0" />
      </template>
    </ElTableColumn>
    <ElTableColumn label="列表" width="52" align="center">
      <template #default="{ row }">
        <ElCheckbox v-model="row.isList" true-value="1" false-value="0" />
      </template>
    </ElTableColumn>
    <ElTableColumn label="查询" width="52" align="center">
      <template #default="{ row }">
        <ElCheckbox v-model="row.isQuery" true-value="1" false-value="0" />
      </template>
    </ElTableColumn>
    <ElTableColumn label="查询方式" width="120">
      <template #default="{ row }">
        <ElSelect v-model="row.queryType" size="small" :disabled="row.isQuery !== '1'">
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
    <ElTableColumn label="必填" width="52" align="center">
      <template #default="{ row }">
        <ElCheckbox v-model="row.isRequired" true-value="1" false-value="0" />
      </template>
    </ElTableColumn>
    <ElTableColumn label="显示类型" width="130">
      <template #default="{ row }">
        <ElSelect v-model="row.htmlType" size="small">
          <ElOption label="文本框" value="input" />
          <ElOption label="文本域" value="textarea" />
          <ElOption label="下拉框" value="select" />
          <ElOption label="单选框" value="radio" />
          <ElOption label="复选框" value="checkbox" />
          <ElOption label="日期控件" value="datetime" />
          <ElOption label="图片上传" value="imageUpload" />
          <ElOption label="文件上传" value="fileUpload" />
          <ElOption label="富文本" value="editor" />
        </ElSelect>
      </template>
    </ElTableColumn>
    <ElTableColumn label="字典类型" min-width="160">
      <template #default="{ row }">
        <ElSelect
          v-model="row.dictType"
          clearable
          filterable
          placeholder="请选择"
          size="small"
          :disabled="!['select', 'radio', 'checkbox'].includes(row.htmlType)"
        >
          <ElOption
            v-for="dict in dictOptions"
            :key="dict.dictType"
            :label="dict.dictName"
            :value="dict.dictType"
          />
        </ElSelect>
      </template>
    </ElTableColumn>
  </ElTable>
</template>

<script setup lang="ts">
  import { ElCheckbox, ElInput, ElOption, ElSelect, ElTable, ElTableColumn } from 'element-plus'
  import type { GenTableColumnItem } from '@/api/generator/gen-external'
  import type { SysDictType } from '@/api/dict/type'

  defineProps<{
    columns: GenTableColumnItem[]
    dictOptions: SysDictType[]
    maxHeight?: number | string
  }>()
</script>
