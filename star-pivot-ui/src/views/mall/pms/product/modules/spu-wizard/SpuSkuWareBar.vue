<template>
  <div v-if="!isEdit" class="sku-ware-bar">
    <span class="sku-ware-bar__label">默认入库仓库</span>
    <ElSelect
      :model-value="defaultWareId"
      clearable
      filterable
      placeholder="选填：发布时写入初始库存"
      style="width: 280px"
      @update:model-value="emit('update:defaultWareId', $event)"
    >
      <ElOption v-for="w in wareOptions" :key="w.id" :label="w.name" :value="w.id!" />
    </ElSelect>
    <span class="sku-ware-bar__hint">填写各 SKU「初始库存」后，保存时将自动入库</span>
  </div>
</template>

<script lang="ts" setup>
  import type { WmsWareInfoVo } from '@/api/mall/wareinfo'

  defineProps<{
    isEdit: boolean
    defaultWareId?: number
    wareOptions: WmsWareInfoVo[]
  }>()

  const emit = defineEmits<{
    'update:defaultWareId': [value?: number]
  }>()
</script>

<style lang="scss" scoped>
  .sku-ware-bar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 12px;
    padding: 10px 12px;
    background: var(--el-fill-color-light);
    border-radius: 8px;

    &__label {
      font-size: 13px;
      color: var(--el-text-color-regular);
    }

    &__hint {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
</style>
