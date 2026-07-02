<!-- C 端专题活动页 -->
<template>
  <div v-loading="loading" class="portal-subject">
    <PortalPageHeader :title="subject.title || '专题活动'" :subtitle="subject.subTitle || '精选好物，限时优惠'">
      <template #extra>
        <ElButton
          v-if="subjectId"
          :type="subjectCollected ? 'danger' : 'default'"
          :loading="collectLoading"
          @click="toggleSubjectCollect"
        >
          {{ subjectCollected ? '已收藏专题' : '收藏专题' }}
        </ElButton>
      </template>
    </PortalPageHeader>

    <div v-if="bannerUrl" class="portal-subject__banner">
      <img :src="bannerUrl" :alt="subject.title" class="portal-subject__banner-img" />
    </div>

    <div v-if="products.length" class="product-grid">
      <div
        v-for="item in products"
        :key="item.id"
        class="product-card"
        @click="goDetail(item.id!)"
      >
        <div class="product-card__img-wrap">
          <img
            :src="coverUrls.get(item.coverImg || '') || placeholderImg"
            :alt="item.spuName"
            class="product-card__img"
          />
        </div>
        <div class="product-card__body">
          <p class="product-card__name">{{ item.spuName }}</p>
          <p v-if="item.brandName" class="product-card__brand">{{ item.brandName }}</p>
          <PortalProductRating :avg-star="item.avgStar" :comment-count="item.commentCount" />
          <p class="product-card__price">
            <span class="currency">¥</span>{{ formatPrice(item.price) }}
          </p>
        </div>
      </div>
    </div>
    <ElEmpty v-else description="暂无商品" />

    <div v-if="total > products.length" class="load-more">
      <ElButton :loading="loadingMore" @click="loadMore">加载更多</ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import {fetchPortalSubjectDetail} from '@/api/portal/subject'
import {
  fetchPortalCollectSubjectAdd,
  fetchPortalCollectSubjectRemove,
  fetchPortalCollectSubjectStatus
} from '@/api/portal/collect'
import type {PortalProductListItem, PortalSubjectDetail} from '@/api/portal/types'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'
import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'
import PortalProductRating from '@/views/portal/components/portal-product-rating.vue'

defineOptions({ name: 'PortalSubject' })

  const route = useRoute()
  const router = useRouter()
  const { requireLogin, portalStore } = usePortalAuth()
  const loading = ref(true)
  const loadingMore = ref(false)
  const subject = ref<PortalSubjectDetail>({})
  const products = ref<PortalProductListItem[]>([])
  const coverUrls = ref(new Map<string, string>())
  const bannerUrl = ref('')
  const pageNum = ref(1)
  const pageSize = 16
  const total = ref(0)
  const subjectId = computed(() => Number(route.params.id) || undefined)
  const subjectCollected = ref(false)
  const collectLoading = ref(false)

  const placeholderImg =
    'data:image/svg+xml,' +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect fill="#f0f0f0" width="200" height="200"/></svg>'
    )

  const formatPrice = (p?: number) => (p != null ? Number(p).toFixed(2) : '--')

  async function resolveCoverImages(items: PortalProductListItem[]) {
    const keys = items.map((item) => item.coverImg).filter(Boolean) as string[]
    if (!keys.length) return
    const map = await resolveGoodsImageDisplayUrls(keys)
    map.forEach((url, key) => coverUrls.value.set(key, url))
  }

  async function resolveBanner(coverImg?: string) {
    if (!coverImg) {
      bannerUrl.value = ''
      return
    }
    const map = await resolveGoodsImageDisplayUrls([coverImg])
    bannerUrl.value = map.get(coverImg) || ''
  }

  async function loadSubject(reset = true) {
    const id = Number(route.params.id)
    if (!Number.isFinite(id)) return
    if (reset) {
      pageNum.value = 1
      products.value = []
    }
    const data = await fetchPortalSubjectDetail(id, {
      pageNum: pageNum.value,
      pageSize
    })
    subject.value = data
    if (reset) {
      await resolveBanner(data.coverImg)
    }
    total.value = data.products?.total || 0
    const rows = data.products?.rows || []
    products.value = reset ? rows : [...products.value, ...rows]
    await resolveCoverImages(rows)
  }

  function loadMore() {
    loadingMore.value = true
    pageNum.value += 1
    loadSubject(false).finally(() => {
      loadingMore.value = false
    })
  }

  function goDetail(id: number) {
    router.push(`/portal/product/${id}`)
  }

  async function loadSubjectCollectStatus() {
    if (!portalStore.isLogin || !subjectId.value) {
      subjectCollected.value = false
      return
    }
    try {
      const res = await fetchPortalCollectSubjectStatus(subjectId.value)
      subjectCollected.value = !!res.collected
    } catch {
      subjectCollected.value = false
    }
  }

  async function toggleSubjectCollect() {
    if (!requireLogin() || !subjectId.value) return
    collectLoading.value = true
    try {
      if (subjectCollected.value) {
        await fetchPortalCollectSubjectRemove(subjectId.value)
        subjectCollected.value = false
      } else {
        await fetchPortalCollectSubjectAdd(subjectId.value)
        subjectCollected.value = true
      }
    } finally {
      collectLoading.value = false
    }
  }

  watch(
    () => route.params.id,
    async () => {
      loading.value = true
      coverUrls.value = new Map()
      try {
        await loadSubject(true)
        await loadSubjectCollectStatus()
      } finally {
        loading.value = false
      }
    },
    { immediate: true }
  )
</script>

<style scoped lang="scss">

  .portal-subject__banner {
    margin-bottom: 20px;
    border-radius: var(--portal-radius-lg);
    overflow: hidden;
    box-shadow: var(--portal-shadow-sm);
  }

  .portal-subject__banner-img {
    display: block;
    width: 100%;
    max-height: 280px;
    object-fit: cover;
  }

  .product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
  }

  .product-card {
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    overflow: hidden;
    cursor: pointer;
    transition: all var(--portal-transition);
    background: var(--portal-bg-elevated);

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--portal-shadow-lg);
      border-color: transparent;
    }

    &__img-wrap {
      aspect-ratio: 1;
      background: #fafbfc;
      overflow: hidden;
    }

    &__img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &__body {
      padding: 14px 16px 16px;
    }

    &__name {
      margin: 0 0 6px;
      font-size: 14px;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      min-height: 42px;
    }

    &__brand {
      margin: 0 0 8px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__price {
      margin: 0;
      color: var(--portal-primary);
      font-size: 20px;
      font-weight: 700;

      .currency {
        font-size: 13px;
      }
    }
  }

  .load-more {
    text-align: center;
    margin-top: 28px;
  }
</style>
