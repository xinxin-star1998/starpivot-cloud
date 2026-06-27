<template>
  <div class="sku-promo-block">
    <div class="sku-promo-row">
      <span class="sku-promo-label">满</span>
      <ElInputNumber
        :min="0"
        :model-value="fullCount"
        class="sku-cell-num sku-cell-num--sm"
        controls-position="right"
        @update:model-value="emit('update:fullCount', $event ?? 0)"
      />
      <span class="sku-promo-label">件</span>
    </div>
    <div class="sku-promo-row">
      <span class="sku-promo-label">打</span>
      <ElInputNumber
        :max="1"
        :min="0"
        :model-value="discount"
        :precision="2"
        :step="0.01"
        class="sku-cell-num sku-cell-num--sm"
        controls-position="right"
        @update:model-value="emit('update:discount', $event ?? 0)"
      />
      <span class="sku-promo-label">折</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  defineProps<{
    fullCount: number
    discount: number
  }>()

  const emit = defineEmits<{
    'update:fullCount': [value: number]
    'update:discount': [value: number]
  }>()
</script>

<style lang="scss" scoped>
  .sku-promo-block {
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-width: max-content;
  }

  .sku-promo-row {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    gap: 6px;
  }

  .sku-promo-label {
    flex-shrink: 0;
    width: 20px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    text-align: center;
  }

  .sku-cell-num--sm {
    width: 100px !important;
    min-width: 100px;
    flex-shrink: 0;
  }
</style>
