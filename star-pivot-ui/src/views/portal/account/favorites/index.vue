<!-- C 端我的收藏 -->
<template>
  <div v-loading="loading" class="portal-favorites">
    <PortalPageHeader title="我的收藏" subtitle="收藏的商品会显示在这里" />

    <div v-if="items.length" class="collect-grid">
      <div v-for="item in items" :key="item.id" class="collect-card">
        <div class="collect-card__img" @click="goDetail(item.spuId!)">
          <img :src="coverUrls.get(item.spuImg || '') || placeholderImg" :alt="item.spuName" />
          <ElTag v-if="item.publishStatus !== 1" type="info" size="small" class="off-tag">已下架</ElTag>
        </div>
        <div class="collect-card__body">
          <p class="name" @click="goDetail(item.spuId!)">{{ item.spuName }}</p>
          <p class="price"><span>¥</span>{{ formatPrice(item.price) }}</p>
          <div class="actions">
            <ElButton
              size="small"
              type="primary"
              plain
              :disabled="item.publishStatus !== 1 || !item.defaultSkuId"
              :loading="addingCartId === item.spuId"
              @click="handleAddCart(item)"
            >
              加购物车
            </ElButton>
            <ElButton size="small" type="primary" :disabled="item.publishStatus !== 1" @click="goDetail(item.spuId!)">
              去看看
            </ElButton>
            <ElButton size="small" :loading="removingId === item.spuId" @click="handleRemove(item.spuId!)">
              取消收藏
            </ElButton>
          </div>
        </div>
      </div>
    </div>
    <ElEmpty v-else description="还没有收藏商品">
      <ElButton type="primary" @click="router.push('/portal')">去逛逛</ElButton>
    </ElEmpty>

    <div v-if="total > items.length" class="load-more">
      <ElButton :loading="loadingMore" @click="loadMore">加载更多</ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import {fetchPortalCollectPageList, fetchPortalCollectRemove} from '@/api/portal/collect'
import {fetchPortalCartAdd} from '@/api/portal/cart'
import type {PortalCollectItem} from '@/api/portal/types'
import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'
import {notifyPortalCartChanged} from '@/utils/portal/cart-event'
import {ElMessage} from 'element-plus'

defineOptions({ name: 'PortalFavorites' })

  const router = useRouter()
  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const loadingMore = ref(false)
  const items = ref<PortalCollectItem[]>([])
  const coverUrls = ref(new Map<string, string>())
  const pageNum = ref(1)
  const pageSize = 12
  const total = ref(0)
  const removingId = ref<number>()
  const addingCartId = ref<number>()

  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="#f0f0f0" width="200" height="200"/></svg>'
    )

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '--')

  async function loadList(reset = true) {
    if (!requireLogin()) return
    if (reset) {
      pageNum.value = 1
      items.value = []
    }
    const res = await fetchPortalCollectPageList({ pageNum: pageNum.value, pageSize })
    total.value = res.total || 0
    const rows = res.rows || []
    items.value = reset ? rows : [...items.value, ...rows]
    const map = await resolveGoodsImageDisplayUrls(
      rows.map((r) => r.spuImg).filter(Boolean) as string[]
    )
    map.forEach((url, key) => coverUrls.value.set(key, url))
  }

  async function handleRemove(spuId: number) {
    removingId.value = spuId
    try {
      await fetchPortalCollectRemove(spuId)
      items.value = items.value.filter((i) => i.spuId !== spuId)
      total.value = Math.max(0, total.value - 1)
    } finally {
      removingId.value = undefined
    }
  }

  function loadMore() {
    loadingMore.value = true
    pageNum.value += 1
    loadList(false).finally(() => {
      loadingMore.value = false
    })
  }

  function goDetail(spuId: number) {
    router.push(`/portal/product/${spuId}`)
  }

  async function handleAddCart(item: PortalCollectItem) {
    if (!item.defaultSkuId) {
      if (item.spuId) goDetail(item.spuId)
      return
    }
    if (!requireLogin()) return
    addingCartId.value = item.spuId
    try {
      await fetchPortalCartAdd({ skuId: item.defaultSkuId, quantity: 1 })
      notifyPortalCartChanged()
      ElMessage.success('已加入购物车')
    } finally {
      addingCartId.value = undefined
    }
  }

  onMounted(async () => {
    try {
      await loadList(true)
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  @import '../styles/variables.scss';

  .collect-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 16px;
  }

  .collect-card {
    display: flex;
    gap: 14px;
    padding: 14px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    background: var(--portal-bg-elevated);

    &__img {
      position: relative;
      width: 96px;
      height: 96px;
      flex-shrink: 0;
      border-radius: var(--portal-radius-sm);
      overflow: hidden;
      cursor: pointer;
      background: #fafbfc;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .off-tag {
        position: absolute;
        top: 4px;
        left: 4px;
      }
    }

    &__body {
      flex: 1;
      min-width: 0;

      .name {
        margin: 0 0 8px;
        font-size: 14px;
        line-height: 1.4;
        cursor: pointer;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;

        &:hover {
          color: var(--portal-primary);
        }
      }

      .price {
        margin: 0 0 12px;
        color: var(--portal-primary);
        font-size: 18px;
        font-weight: 700;

        span {
          font-size: 12px;
        }
      }

      .actions {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
  }

  .load-more {
    text-align: center;
    margin-top: 24px;
  }
</style>
