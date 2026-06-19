<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    :show-reset="true"
    :show-search="true"
    @reset="handleReset"
    @search="handleSearch"
  >
  </ArtSearchBar>
</template>

<script setup lang="ts">
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import { OPER_BUSINESS_TYPE_OPTIONS } from '../constants'

  interface Props {
    modelValue: Record<string, any>
  }
  interface Emits {
    (e: 'update:modelValue', value: Record<string, any>): void
    (e: 'search', params: Record<string, any>): void
    (e: 'reset'): void
  }
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 表单数据双向绑定
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val: Record<string, any>) => emit('update:modelValue', val)
  })

  // 校验规则
  const rules = {}

  // 操作状态选项
  const statusOptions = [
    { label: '正常', value: 0 },
    { label: '异常', value: 1 }
  ]

  // 表单配置
  const formItems = computed(() => [
    {
      label: '模块标题',
      key: 'title',
      type: 'input',
      placeholder: '请输入模块标题',
      clearable: true
    },
    {
      label: '业务类型',
      key: 'businessType',
      type: 'select',
      props: {
        placeholder: '请选择业务类型',
        clearable: true,
        options: OPER_BUSINESS_TYPE_OPTIONS
      }
    },
    {
      label: '操作人员',
      key: 'operName',
      type: 'input',
      placeholder: '请输入操作人员',
      clearable: true
    },
    {
      label: '操作状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择操作状态',
        clearable: true,
        options: statusOptions
      }
    },
    {
      label: '操作时间',
      key: 'dateRange',
      type: 'datetimerange',
      span: 8,
      props: {
        type: 'datetimerange',
        rangeSeparator: '至',
        startPlaceholder: '开始时间',
        endPlaceholder: '结束时间',
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        style: 'width: 100%',
        clearable: true
      }
    }
  ])

  // 事件处理
  function handleReset() {
    emit('reset')
  }

  async function handleSearch() {
    await searchBarRef.value.validate()
    // 处理日期范围，将 dateRange 拆分为 startTime 和 endTime
    const searchParams = { ...formData.value }
    if (
      searchParams.dateRange &&
      Array.isArray(searchParams.dateRange) &&
      searchParams.dateRange.length === 2
    ) {
      searchParams.startTime = searchParams.dateRange[0]
      searchParams.endTime = searchParams.dateRange[1]
    } else {
      searchParams.startTime = undefined
      searchParams.endTime = undefined
    }
    // 删除 dateRange，因为后端不需要这个字段
    delete searchParams.dateRange
    emit('search', searchParams)
  }
</script>
