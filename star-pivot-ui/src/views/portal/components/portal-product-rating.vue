<!-- 商品卡片评分 -->
<template>
  <div v-if="commentCount" class="portal-product-rating">
    <ElRate :model-value="avgValue" disabled />
    <span class="portal-product-rating__count">{{ commentCount }}条</span>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'PortalProductRating' })

  const props = defineProps<{
    avgStar?: number
    commentCount?: number
  }>()

  const avgValue = computed(() => {
    const avg = props.avgStar
    return avg != null ? Number(avg) : 0
  })
</script>

<style scoped lang="scss">
  .portal-product-rating {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 4px;

    :deep(.el-rate) {
      height: 16px;
    }

    :deep(.el-rate__icon) {
      font-size: 12px;
      margin-right: 2px;
    }

    &__count {
      font-size: 11px;
      color: var(--portal-text-muted);
    }
  }
</style>
