<template>
  <div
    ref="referenceRef"
    @focusin="props.manual ? undefined : handleFocus"
    @click="props.manual ? undefined : handleClick"
  >
    <slot></slot>
  </div>

  <ElDialog
    v-model="visible"
    :show-close="false"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    width="860px"
    append-to-body
    class="icon-picker-dialog"
    @closed="resetSearch"
  >
    <template #header>
      <div class="picker-titlebar">
        <div class="picker-title">选择图标</div>
        <button class="picker-close" type="button" aria-label="close" @click="handleClose">
          <ElIcon><Close /></ElIcon>
        </button>
      </div>
    </template>

    <div class="icon-picker">
      <ElTabs v-model="activeIconSet" class="picker-tabs" @tab-change="handleIconSetChange">
        <ElTabPane
          v-for="set in iconSets"
          :key="set.prefix"
          :label="set.label"
          :name="set.prefix"
        />
      </ElTabs>

      <div class="picker-toolbar">
        <ElInput
          ref="searchInputRef"
          v-model="iconSearchText"
          placeholder="支持中文或英文，如 用户、设置、home"
          clearable
          class="picker-search"
          @input="handleIconSearch"
          @clear="handleSearchClear"
          @keyup.enter="handleSearchEnter"
        >
          <template #prefix>
            <ElIcon><Search /></ElIcon>
          </template>
        </ElInput>

        <div class="picker-meta">
          <span class="picker-meta-pill">{{ iconSetLabel }}</span>
          <span class="picker-meta-count">共 {{ totalIcons }} 个</span>
        </div>
      </div>

      <div v-if="isSearchingIcons" class="icon-loading">
        <ElIcon class="is-loading"><Loading /></ElIcon>
        <span style="margin-left: 8px">搜索中...</span>
      </div>
      <div v-if="isSwitchingIconSet" class="icon-loading">
        <ElIcon class="is-loading"><Loading /></ElIcon>
        <span style="margin-left: 8px">加载图标库...</span>
      </div>
      <div v-if="showEmptyState" class="icon-empty">{{ emptySearchHint }}</div>

      <div ref="pickerBodyRef" class="picker-body" @scroll="handlePickerScroll">
        <div class="icon-grid">
          <button
            v-for="iconItem in displayedIcons"
            :key="iconItem"
            class="icon-card"
            type="button"
            :class="{ active: (props.modelValue || '') === iconItem }"
            :title="iconItem"
            @click="selectIcon(iconItem)"
          >
            <Icon :icon="iconItem" class="icon-card-icon" />
          </button>
        </div>
      </div>

      <div class="picker-footer">
        <ElButton @click="handleClose">关闭</ElButton>
      </div>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
import {ElButton, ElDialog, ElIcon, ElInput, ElTabPane, ElTabs} from 'element-plus'
import {Close, Loading, Search} from '@element-plus/icons-vue'
import {Icon} from '@iconify/vue'
import {DEFAULT_ICON_SETS, ensureIconCollection, type OfflineIconSetOption} from '@/utils/ui/iconify-offline'
import {
  containsCjk,
  expandIconSearchKeywords,
  iconMatchesSearchTerms,
  toIconifySearchQuery
} from '@/utils/ui/icon-search-zh'

type IconSet = OfflineIconSetOption

  interface Props {
    modelValue: string | undefined
    /** 手动触发模式：不再监听 focusin/click 自动打开，仅通过 expose 的 open() 打开 */
    manual?: boolean
    /** 可选：自定义可切换的图标库（Iconify prefix 列表） */
    iconSets?: IconSet[]
  }

  interface Emits {
    (e: 'update:modelValue', value: string): void
  }

  // 使用 props 以便在模板中展示当前已选图标
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const visible = ref(false)
  const referenceRef = ref<HTMLElement>()
  const searchInputRef = ref<InstanceType<typeof ElInput>>()
  const pickerBodyRef = ref<HTMLElement>()

  // 处理输入框聚焦
  const handleFocus = () => {
    // 使用 nextTick 确保在 DOM 更新后打开
    nextTick(() => {
      visible.value = true
    })
  }

  // 处理点击事件，阻止冒泡并打开选择器
  const handleClick = (event: MouseEvent) => {
    event.stopPropagation()
    visible.value = true
  }

  /**
   * 监听弹窗显示状态，打开时自动聚焦搜索输入框
   */
  watch(visible, (newVal) => {
    if (newVal) {
      // 弹窗打开后，延迟聚焦输入框，确保 DOM 已渲染
      nextTick(() => {
        setTimeout(() => {
          searchInputRef.value?.focus()
        }, 100)
      })
    }
  })

  // 暴露方法供外部调用
  defineExpose({
    open: () => {
      visible.value = true
    },
    close: () => {
      visible.value = false
    }
  })

  const iconSets = computed<IconSet[]>(() => props.iconSets ?? DEFAULT_ICON_SETS)

  const activeIconSet = ref<string>(iconSets.value[0]?.prefix ?? 'heroicons-outline')
  const iconSetLabel = computed(() => {
    const found = iconSets.value.find((s) => s.prefix === activeIconSet.value)
    return found?.label ?? activeIconSet.value
  })
  const allIcons = ref<string[]>([])
  const isSwitchingIconSet = ref(false)

  const loadedIconSets = new Set<string>()

  const loadIconifyIcons = async (prefix: string) => {
    try {
      if (
        loadedIconSets.has(prefix) &&
        allIcons.value.length > 0 &&
        allIcons.value[0]?.startsWith(`${prefix}:`)
      ) {
        return
      }

      isSwitchingIconSet.value = true
      await nextTick()
      await new Promise((r) => setTimeout(r, 0))

      allIcons.value = await ensureIconCollection(prefix)
      if (allIcons.value.length > 0) {
        loadedIconSets.add(prefix)
      }
    } catch (e) {
      console.warn(`未能加载 ${prefix} 离线数据，将回退到在线搜索模式。`, e)
      allIcons.value = []
    } finally {
      isSwitchingIconSet.value = false
    }
  }

  onMounted(() => {
    loadIconifyIcons(activeIconSet.value)
  })

  /**
   * 图标搜索功能 composable
   * 负责管理搜索状态、本地搜索和在线搜索逻辑
   */
  const useIconSearch = () => {
    // 搜索相关状态
    const iconSearchText = ref('')
    const isSearchingIcons = ref(false)
    const localSearchResults = ref<string[]>([])
    const onlineSearchResults = ref<string[]>([])
    const searchError = ref<string | null>(null)

    /**
     * 本地搜索图标
     * 优先在 Remix Icon 全量列表中搜索；若未加载则在空列表中返回空结果
     */
    const searchLocalIcons = (keyword: string): string[] => {
      if (!keyword || keyword.trim().length === 0) {
        return allIcons.value
      }
      const terms = expandIconSearchKeywords(keyword)
      if (!terms.length) {
        return []
      }
      return allIcons.value.filter((icon) => iconMatchesSearchTerms(icon, terms))
    }

    /**
     * 在线搜索图标
     * 当离线图标未加载时，从 Iconify Remix Icon 前缀在线搜索兜底
     */
    const searchIconsOnline = async (query: string): Promise<void> => {
      if (!query || query.trim().length < 2) {
        onlineSearchResults.value = []
        searchError.value = null
        return
      }

      isSearchingIcons.value = true
      searchError.value = null

      try {
        const prefix = activeIconSet.value
        const response = await fetch(
          `https://api.iconify.design/search?query=${encodeURIComponent(query)}&limit=300&prefixes=${encodeURIComponent(prefix)}`
        )
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        const data = await response.json()
        onlineSearchResults.value = data.icons && Array.isArray(data.icons) ? data.icons : []
      } catch (error) {
        console.error('搜索图标失败:', error)
        searchError.value = '在线搜索失败，请稍后重试'
        onlineSearchResults.value = []
      } finally {
        isSearchingIcons.value = false
      }
    }

    /**
     * 执行搜索
     * 先进行本地搜索，如果关键词长度>=2则同时进行在线搜索
     */
    const performSearch = async (keyword: string): Promise<void> => {
      const trimmedKeyword = keyword.trim()
      if (!trimmedKeyword) {
        localSearchResults.value = []
        onlineSearchResults.value = []
        searchError.value = null
        return
      }

      const terms = expandIconSearchKeywords(trimmedKeyword)
      if (containsCjk(trimmedKeyword) && !terms.length) {
        localSearchResults.value = []
        onlineSearchResults.value = []
        searchError.value = '未匹配到该中文关键词，请换词或尝试英文（如 home、user）'
        return
      }

      searchError.value = null
      localSearchResults.value = searchLocalIcons(trimmedKeyword)

      const onlineQuery = toIconifySearchQuery(terms) || trimmedKeyword
      const needOnline =
        onlineQuery.length >= 2 &&
        (localSearchResults.value.length === 0 || containsCjk(trimmedKeyword))

      if (needOnline) {
        await searchIconsOnline(onlineQuery)
      } else {
        onlineSearchResults.value = []
        isSearchingIcons.value = false
      }
    }

    /**
     * 重置搜索状态
     * @param keepKeyword 切换图标库时保留输入框关键词
     */
    const resetSearch = (opts?: { keepKeyword?: boolean }): void => {
      if (!opts?.keepKeyword) {
        iconSearchText.value = ''
      }
      localSearchResults.value = []
      onlineSearchResults.value = []
      isSearchingIcons.value = false
      searchError.value = null
    }

    /**
     * 合并搜索结果
     * 优先显示本地搜索结果，然后显示在线搜索结果，并去重
     */
    const mergedSearchResults = computed(() => {
      const local = localSearchResults.value
      const online = onlineSearchResults.value

      // 如果只有本地结果，直接返回
      if (online.length === 0) {
        return local
      }

      // 合并结果并去重，本地结果优先
      const allResults = [...local, ...online]
      const uniqueResults = Array.from(new Set(allResults))
      return uniqueResults
    })

    /**
     * 是否有搜索结果
     */
    const hasSearchResults = computed(() => {
      return mergedSearchResults.value.length > 0
    })

    /**
     * 是否显示空状态
     */
    const showEmptyState = computed(() => {
      const keyword = iconSearchText.value.trim()
      return keyword.length > 0 && !isSearchingIcons.value && !hasSearchResults.value
    })

    const emptySearchHint = computed(() => {
      if (searchError.value) return searchError.value
      const keyword = iconSearchText.value.trim()
      if (containsCjk(keyword)) {
        return '未找到相关图标，可换关键词或尝试英文'
      }
      return '未找到相关图标'
    })

    return {
      iconSearchText,
      isSearchingIcons,
      localSearchResults,
      onlineSearchResults,
      searchError,
      mergedSearchResults,
      hasSearchResults,
      showEmptyState,
      emptySearchHint,
      performSearch,
      resetSearch
    }
  }

  // 使用图标搜索功能
  const {
    iconSearchText,
    isSearchingIcons,
    mergedSearchResults,
    showEmptyState,
    emptySearchHint,
    performSearch,
    resetSearch
  } = useIconSearch()

  const handleIconSetChange = async () => {
    const keyword = iconSearchText.value.trim()
    resetSearch({ keepKeyword: true })
    resetDisplayLimit()
    await loadIconifyIcons(activeIconSet.value)
    if (keyword) {
      await performSearch(keyword)
    }
  }

  // 防抖搜索处理
  let searchTimer: ReturnType<typeof setTimeout> | null = null
  const handleIconSearch = (): void => {
    if (searchTimer) {
      clearTimeout(searchTimer)
    }

    searchTimer = setTimeout(() => {
      performSearch(iconSearchText.value)
    }, 500)
  }

  /**
   * 处理搜索框清空
   * 当用户点击清空按钮时，立即重置搜索状态
   */
  const handleSearchClear = (): void => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
    resetSearch()
  }

  /**
   * 处理回车键搜索
   * 按回车键时立即执行搜索，跳过防抖延迟
   */
  const handleSearchEnter = (): void => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
    const keyword = iconSearchText.value.trim()
    if (keyword.length > 0) {
      performSearch(keyword)
    } else {
      resetSearch()
    }
  }

  // 过滤图标列表（用于模板显示）
  const filteredIcons = computed(() => {
    const keyword = iconSearchText.value.trim()
    if (keyword.length > 0) return mergedSearchResults.value
    // 默认展示全量（离线）列表；若离线未加载，则展示空（等待用户搜索触发在线兜底）
    return allIcons.value
  })

  // 分批渲染，避免一次性渲染几千个按钮导致切换卡顿
  const displayLimit = ref(300)
  const resetDisplayLimit = () => {
    displayLimit.value = 300
    // 回到顶部，避免切换后仍停留在底部造成“看似空白”
    if (pickerBodyRef.value) pickerBodyRef.value.scrollTop = 0
  }

  watch(
    () => [activeIconSet.value, iconSearchText.value],
    () => {
      resetDisplayLimit()
    }
  )

  const displayedIcons = computed(() => {
    return filteredIcons.value.slice(0, displayLimit.value)
  })

  const handlePickerScroll = () => {
    const el = pickerBodyRef.value
    if (!el) return
    // 接近底部时加载更多
    const nearBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 200
    if (nearBottom) {
      displayLimit.value = Math.min(filteredIcons.value.length, displayLimit.value + 300)
    }
  }

  // 清理搜索定时器
  onUnmounted(() => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
  })

  // 选择图标
  const selectIcon = (icon: string) => {
    emit('update:modelValue', icon)
    // 延迟关闭，确保值已更新
    setTimeout(() => {
      visible.value = false
      resetSearch()
    }, 100)
  }

  const handleClose = () => {
    visible.value = false
    resetSearch()
  }

  const totalIcons = computed(() => {
    return allIcons.value.length > 0 ? allIcons.value.length : mergedSearchResults.value.length
  })
</script>

<style scoped lang="scss">
  .icon-picker {
    padding: 6px 10px 12px;
    background: #fff;
    border-radius: 12px;
  }

  .picker-titlebar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: 4px;
  }

  .picker-title {
    font-size: 20px;
    font-weight: 600;
    line-height: 30px;
    color: #303133;
  }

  .picker-close {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    cursor: pointer;
    background: transparent;
    border: none;
    border-radius: 8px;
    transition: background 0.15s;

    &:hover {
      background: rgb(0 0 0 / 4%);
    }
  }

  .picker-toolbar {
    display: flex;
    gap: 12px;
    align-items: center;
    padding: 10px 12px;
    background: #f5f7fa;
    border: 1px solid #ebeef5;
    border-radius: 10px;
  }

  .picker-tabs {
    margin-bottom: 10px;
  }

  .picker-search {
    flex: 1;
  }

  .picker-meta {
    display: inline-flex;
    gap: 10px;
    align-items: center;
    white-space: nowrap;
  }

  .picker-meta-pill {
    display: inline-flex;
    align-items: center;
    height: 28px;
    padding: 0 10px;
    font-size: 12px;
    color: #606266;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 999px;
  }

  .picker-meta-count {
    font-size: 12px;
    color: #909399;
  }

  .icon-loading,
  .icon-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    font-size: 14px;
    color: #909399;
  }

  .picker-body {
    height: 520px;
    padding-top: 12px;
    overflow: auto;
  }

  .icon-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(76px, 1fr));
    gap: 12px;
    padding: 2px;
  }

  .icon-card {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 56px;
    cursor: pointer;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    transition: all 0.15s;

    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 10px rgb(64 158 255 / 16%);
      transform: translateY(-1px);
    }

    &.active {
      border-color: #409eff;
      box-shadow: 0 2px 10px rgb(64 158 255 / 22%);
    }
  }

  .icon-card-icon {
    font-size: 22px;
    color: #303133;
  }

  .icon-card.active .icon-card-icon {
    color: #409eff;
  }

  .picker-footer {
    display: flex;
    justify-content: flex-end;
    padding-top: 12px;
  }
</style>

<style lang="scss">
  .icon-picker-dialog {
    .el-dialog__header {
      margin-right: 0;
      padding: 4px 16px 0;
    }

    .el-dialog__body {
      padding: 0 16px 12px;
    }
  }
</style>
