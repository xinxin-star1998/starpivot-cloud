<!-- C 端待评价 -->
<template>
  <div v-loading="loading" class="portal-pending-reviews">
    <PortalPageHeader title="待评价" subtitle="购买并已收货的商品，欢迎分享使用感受" />

    <div v-if="items.length" class="pending-list">
      <div v-for="item in items" :key="`${item.orderSn}-${item.spuId}`" class="pending-card">
        <img
          :src="coverUrls.get(item.coverImg || '') || placeholderImg"
          class="pending-card__img"
          alt=""
          @click="goReview(item.spuId!)"
        />
        <div class="pending-card__body">
          <p class="pending-card__order">订单 {{ item.orderSn }}</p>
          <p class="pending-card__name" @click="goReview(item.spuId!)">{{ item.spuName }}</p>
          <ElButton type="primary" size="small" @click="goReview(item.spuId!)">去评价</ElButton>
        </div>
      </div>
    </div>
    <ElEmpty v-else description="暂无待评价商品">
      <ElButton type="primary" @click="router.push('/portal/orders')">查看订单</ElButton>
    </ElEmpty>
  </div>
</template>

<script setup lang="ts">
import {fetchPortalPendingReviews} from '@/api/portal/comment'
import type {PortalPendingReview} from '@/api/portal/types'
import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'

defineOptions({ name: 'PortalPendingReviews' })

  const router = useRouter()
  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const items = ref<PortalPendingReview[]>([])
  const coverUrls = ref(new Map<string, string>())

  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="#f0f0f0" width="200" height="200"/></svg>'
    )

  function goReview(spuId: number) {
    router.push(`/portal/product/${spuId}?review=1`)
  }

  onMounted(async () => {
    if (!requireLogin()) {
      loading.value = false
      return
    }
    try {
      items.value = await fetchPortalPendingReviews()
      const keys = items.value.map((i) => i.coverImg).filter(Boolean) as string[]
      if (keys.length) {
        const map = await resolveGoodsImageDisplayUrls(keys)
        map.forEach((url, key) => coverUrls.value.set(key, url))
      }
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">

  .pending-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .pending-card {
    display: flex;
    gap: 14px;
    padding: 14px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    background: var(--portal-bg-elevated);

    &__img {
      width: 88px;
      height: 88px;
      object-fit: cover;
      border-radius: var(--portal-radius-sm);
      cursor: pointer;
      background: #fafbfc;
    }

    &__body {
      flex: 1;
      min-width: 0;
    }

    &__order {
      margin: 0 0 6px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__name {
      margin: 0 0 12px;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      line-height: 1.4;

      &:hover {
        color: var(--portal-primary);
      }
    }
  }
</style>
