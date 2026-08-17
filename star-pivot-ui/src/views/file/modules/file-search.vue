<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  />
</template>

<script setup lang="ts">
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'

  const props = defineProps<{
    modelValue: Record<string, unknown>
    recycle?: boolean
  }>()

  const emit = defineEmits<{
    'update:modelValue': [value: Record<string, unknown>]
    search: []
    reset: []
  }>()

  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const rules = {}

  const formItems = computed(() => {
    const items = [
      {
        label: '文件名',
        key: 'fileName',
        type: 'input',
        placeholder: '请输入文件名',
        clearable: true
      },
      {
        label: props.recycle ? '删除人' : '上传人',
        key: props.recycle ? 'deleteBy' : 'createBy',
        type: 'input',
        placeholder: props.recycle ? '请输入删除人' : '请输入上传人',
        clearable: true
      },
      {
        label: props.recycle ? '删除时间' : '上传时间',
        key: 'timeRange',
        type: 'datetimerange',
        props: {
          type: 'datetimerange',
          valueFormat: 'YYYY-MM-DD HH:mm:ss',
          startPlaceholder: '开始时间',
          endPlaceholder: '结束时间',
          clearable: true
        }
      }
    ]
    if (props.recycle) {
      items.splice(1, 0, {
        label: '业务分类',
        key: 'category',
        type: 'select',
        props: {
          placeholder: '全部分类',
          clearable: true,
          options: [
            { label: '系统通用', value: 'SYSTEM' },
            { label: '办公审批', value: 'OA' },
            { label: '合同档案', value: 'CONTRACT' },
            { label: '资质证件', value: 'CERT' },
            { label: '项目资料', value: 'PROJECT' },
            { label: '客户资料', value: 'CUSTOMER' },
            { label: '商品素材', value: 'GOODS' },
            { label: '财务单据', value: 'FINANCE' },
            { label: '人事档案', value: 'HR' },
            { label: '其他附件', value: 'OTHER' }
          ]
        }
      })
    }
    return items
  })

  function handleReset() {
    emit('reset')
  }

  async function handleSearch() {
    await searchBarRef.value?.validate()
    emit('search')
  }
</script>
