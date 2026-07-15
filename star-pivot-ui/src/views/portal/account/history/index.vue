<!-- C 端浏览足迹 -->
<template>
  <div class="portal-browse-history">
    <PortalPageHeader title="浏览足迹" subtitle="最近浏览过的商品">
      <template v-if="items.length" #extra>
        <ElButton link type="danger" @click="handleClearAll">清空足迹</ElButton>
      </template>
    </PortalPageHeader>

    <div v-if="items.length" class="history-list">
      <div v-for="item in items" :key="item.spuId" class="history-card">
        <img
          :src="coverUrls.get(item.coverImg || '') || placeholderImg"
          class="history-card__img"
          alt=""
          @click="goDetail(item.spuId)"
        />
        <div class="history-card__body">
          <p class="history-card__name" @click="goDetail(item.spuId)">{{ item.spuName || `商品 #${item.spuId}` }}</p>
          <p v-if="item.price != null" class="history-card__price">¥{{ formatPrice(item.price) }}</p>
          <p class="history-card__time">{{ formatTime(item.viewedAt) }}</p>
        </div>
        <ElButton link type="danger" @click="handleRemove(item.spuId)">删除</ElButton>
      </div>
    </div>
    <ElEmpty v-else description="还没有浏览记录">
      <ElButton type="primary" @click="router.push('/portal')">去逛逛</ElButton>
    </ElEmpty>
  </div>
</template>

<script setup lang="ts">
import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'
import {
  clearPortalBrowseHistory,
  getPortalBrowseHistory,
  type PortalBrowseRecord,
  removePortalBrowseRecord
} from '@/utils/portal/browse-history'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'
import {formatMoney} from '@/utils/mall/money'
import {ElMessageBox} from 'element-plus'

defineOptions({ name: 'PortalBrowseHistory' })

  const router = useRouter()
  const items = ref<PortalBrowseRecord[]>([])
  const coverUrls = ref(new Map<string, string>())

  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="#f0f0f0" width="200" height="200"/></svg>'
    )

  const formatPrice = (p?: number) => formatMoney(p)

  function formatTime(ts: number) {
    const date = new Date(ts)
    if (Number.isNaN(date.getTime())) return ''
    const now = new Date()
    const sameDay =
      date.getFullYear() === now.getFullYear() &&
      date.getMonth() === now.getMonth() &&
      date.getDate() === now.getDate()
    if (sameDay) {
      return `今天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    }
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }

  async function loadList() {
    items.value = getPortalBrowseHistory()
    const keys = items.value.map((item) => item.coverImg).filter(Boolean) as string[]
    if (keys.length) {
      const map = await resolveGoodsImageDisplayUrls(keys)
      map.forEach((url, key) => coverUrls.value.set(key, url))
    }
  }

  function goDetail(spuId: number) {
    router.push(`/portal/product/${spuId}`)
  }

  async function handleRemove(spuId: number) {
    removePortalBrowseRecord(spuId)
    await loadList()
  }

  async function handleClearAll() {
    await ElMessageBox.confirm('确定清空全部浏览足迹吗？', '提示', { type: 'warning' })
    clearPortalBrowseHistory()
    items.value = []
    coverUrls.value = new Map()
  }

  onMounted(() => {
    loadList()
  })
</script>

<style scoped lang="scss">

  .history-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .history-card {
    display: flex;
    align-items: center;
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
      flex-shrink: 0;
    }

    &__body {
      flex: 1;
      min-width: 0;
    }

    &__name {
      margin: 0 0 6px;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      line-height: 1.4;

      &:hover {
        color: var(--portal-primary);
      }
    }

    &__price {
      margin: 0 0 4px;
      color: var(--portal-primary);
      font-weight: 700;
    }

    &__time {
      margin: 0;
      font-size: 12px;
      color: var(--portal-text-muted);
    }
  }
</style>
