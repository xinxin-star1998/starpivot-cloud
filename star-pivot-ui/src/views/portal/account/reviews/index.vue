<!-- C 端我的评价 -->
<template>
  <div v-loading="loading" class="portal-reviews">
    <PortalPageHeader title="我的评价" subtitle="查看您发表过的商品评价" />

    <div v-if="items.length" class="review-list">
      <div v-for="item in items" :key="item.id" class="review-card">
        <div class="review-card__head">
          <button type="button" class="spu-link" @click="goProduct(item.spuId!)">
            {{ item.spuName || `商品 #${item.spuId}` }}
          </button>
          <ElRate :model-value="item.star" disabled />
          <span class="time">{{ item.createTime }}</span>
        </div>
        <p v-if="item.spuAttributes" class="review-card__sku">{{ item.spuAttributes }}</p>
        <p class="review-card__content">{{ item.content }}</p>
        <div v-if="imageMap.get(item.id!)" class="review-card__images">
          <img
            v-for="(url, idx) in imageMap.get(item.id!) || []"
            :key="idx"
            :src="url"
            alt="评价图"
            @click="previewImage(url)"
          />
        </div>
      </div>
    </div>
    <ElEmpty v-else description="还没有发表评价">
      <ElButton type="primary" @click="router.push('/portal/account/pending-reviews')">去评价</ElButton>
    </ElEmpty>

    <div v-if="total > items.length" class="load-more">
      <ElButton :loading="loadingMore" @click="loadMore">加载更多</ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import {fetchPortalMyCommentPageList} from '@/api/portal/comment'
import type {PortalComment} from '@/api/portal/types'
import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {resolveCommentResourceUrls} from '@/utils/portal/comment-resources'

defineOptions({ name: 'PortalReviews' })

  const router = useRouter()
  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const loadingMore = ref(false)
  const items = ref<PortalComment[]>([])
  const imageMap = ref(new Map<number, string[]>())
  const pageNum = ref(1)
  const pageSize = 10
  const total = ref(0)

  async function resolveImages(rows: PortalComment[]) {
    for (const row of rows) {
      if (!row.id || !row.resources) continue
      const urls = await resolveCommentResourceUrls(row.resources)
      if (urls.length) imageMap.value.set(row.id, urls)
    }
  }

  async function loadList(reset = true) {
    if (!requireLogin()) return
    if (reset) {
      pageNum.value = 1
      items.value = []
      imageMap.value = new Map()
    }
    const res = await fetchPortalMyCommentPageList({ pageNum: pageNum.value, pageSize })
    total.value = res.total || 0
    const rows = res.rows || []
    items.value = reset ? rows : [...items.value, ...rows]
    await resolveImages(rows)
  }

  function loadMore() {
    loadingMore.value = true
    pageNum.value += 1
    loadList(false).finally(() => {
      loadingMore.value = false
    })
  }

  function goProduct(spuId: number) {
    router.push(`/portal/product/${spuId}`)
  }

  function previewImage(url: string) {
    window.open(url, '_blank', 'noopener')
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
  @import '../../styles/variables.scss';

  .review-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .review-card {
    padding: 18px 20px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    background: var(--portal-bg-elevated);

    &__head {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      flex-wrap: wrap;

      .spu-link {
        border: none;
        background: none;
        padding: 0;
        font-size: 15px;
        font-weight: 600;
        color: var(--portal-text);
        cursor: pointer;

        &:hover {
          color: var(--portal-primary);
        }
      }

      .time {
        margin-left: auto;
        font-size: 12px;
        color: var(--portal-text-muted);
      }
    }

    &__sku {
      margin: 0 0 6px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__content {
      margin: 0;
      font-size: 14px;
      line-height: 1.6;
      color: var(--portal-text-secondary);
    }

    &__images {
      display: flex;
      gap: 8px;
      margin-top: 12px;
      flex-wrap: wrap;

      img {
        width: 72px;
        height: 72px;
        object-fit: cover;
        border-radius: var(--portal-radius-sm);
        cursor: pointer;
        border: 1px solid var(--portal-border);
      }
    }
  }

  .load-more {
    text-align: center;
    margin-top: 24px;
  }
</style>
