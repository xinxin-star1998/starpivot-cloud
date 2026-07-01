<!-- 最近搜索 + 热门搜索 -->
<template>
  <section class="portal-search-hints" :class="{ 'portal-search-hints--compact': compact }">
    <div v-if="history.length" class="hint-block">
      <div class="hint-block__head">
        <span>最近搜索</span>
        <button type="button" class="hint-clear" @click="handleClear">清空</button>
      </div>
      <div class="hint-tags">
        <button
          v-for="item in history"
          :key="item"
          type="button"
          class="hint-tag"
          @click="emit('select', item)"
        >
          {{ item }}
        </button>
      </div>
    </div>
    <div class="hint-block">
      <div class="hint-block__head">
        <span>热门搜索</span>
      </div>
      <div class="hint-tags">
        <button
          v-for="item in PORTAL_HOT_SEARCH_KEYWORDS"
          :key="item"
          type="button"
          class="hint-tag hint-tag--hot"
          @click="emit('select', item)"
        >
          {{ item }}
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import {
  clearPortalSearchHistory,
  getPortalSearchHistory,
  PORTAL_HOT_SEARCH_KEYWORDS
} from '@/utils/portal/search-history'

defineOptions({ name: 'PortalSearchHints' })

  withDefaults(
    defineProps<{
      compact?: boolean
    }>(),
    {
      compact: false
    }
  )

  const emit = defineEmits<{
    select: [keyword: string]
  }>()

  const history = ref<string[]>([])

  function refreshHistory() {
    history.value = getPortalSearchHistory()
  }

  function handleClear() {
    clearPortalSearchHistory()
    refreshHistory()
  }

  function onHistoryChanged() {
    refreshHistory()
  }

  onMounted(() => {
    refreshHistory()
    window.addEventListener('portal-search-history-changed', onHistoryChanged)
  })

  onUnmounted(() => {
    window.removeEventListener('portal-search-history-changed', onHistoryChanged)
  })
</script>

<style scoped lang="scss">
  .portal-search-hints {
    margin-top: 14px;
    margin-bottom: 20px;

    &--compact {
      margin-top: 12px;

      .hint-block {
        margin-bottom: 12px;
      }

      .hint-tag {
        padding: 5px 12px;
        font-size: 12px;
      }
    }
  }

  .hint-block {
    margin-bottom: 16px;

    &__head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;
      font-size: 14px;
      font-weight: 600;
      color: var(--portal-text);
    }
  }

  .hint-clear {
    padding: 0;
    border: none;
    background: none;
    color: var(--portal-text-muted);
    font-size: 12px;
    cursor: pointer;

    &:hover {
      color: var(--portal-primary);
    }
  }

  .hint-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .hint-tag {
    padding: 6px 14px;
    border: 1px solid var(--portal-border);
    border-radius: 16px;
    background: var(--portal-bg-elevated);
    color: var(--portal-text-secondary);
    font-size: 13px;
    cursor: pointer;
    transition: all var(--portal-transition);

    &:hover {
      border-color: var(--portal-primary);
      color: var(--portal-primary);
      background: var(--portal-primary-light);
    }

    &--hot {
      border-color: #ffd6d6;
      background: #fff8f8;
    }
  }
</style>
