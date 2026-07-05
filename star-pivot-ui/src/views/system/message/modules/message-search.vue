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
interface Props {
  modelValue: {
    readFlag?: string
    msgType?: string
  }
}

interface Emits {
  (e: 'update:modelValue', value: Props['modelValue']): void
  (e: 'search', params: Props['modelValue']): void
  (e: 'reset'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const searchBarRef = ref()
const formData = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const rules = {}

const formItems = computed(() => [
  {
    label: '已读状态',
    key: 'readFlag',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '未读', value: '0' },
        { label: '已读', value: '1' }
      ]
    }
  },
  {
    label: '消息类型',
    key: 'msgType',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '审批待办', value: 'APPROVAL_TASK' },
        { label: '审批结果', value: 'APPROVAL_RESULT' },
        { label: '退款告警', value: 'REFUND_ALERT' },
        { label: '系统群发', value: 'BROADCAST' },
        { label: '系统通知', value: 'SYSTEM' },
        { label: '订单消息', value: 'ORDER' }
      ]
    }
  }
])

function handleReset() {
  emit('reset')
}

async function handleSearch() {
  await searchBarRef.value?.validate?.()
  emit('search', { ...formData.value })
}
</script>
