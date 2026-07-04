<!-- 缓存管理页面 -->
<template>
  <div class="redis-page art-full-height">
    <ElCard class="art-table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>缓存管理</span>
          <ElButton type="primary" :icon="Refresh" @click="handleRefresh" :loading="loading">
            刷新
          </ElButton>
        </div>
      </template>

      <div class="cache-container">
        <!-- 左侧：缓存列表 -->
        <div class="cache-list-panel">
          <ElCard shadow="hover" class="panel-card">
            <template #header>
              <div class="panel-header">
                <span>缓存列表</span>
                <ElButton
                  :icon="Refresh"
                  circle
                  size="small"
                  @click="refreshCacheList"
                  :loading="loadingCacheList"
                />
              </div>
            </template>
            <ElTable
              ref="cacheTableRef"
              :data="cacheList"
              row-key="cacheName"
              highlight-current-row
              @current-change="handleCacheSelect"
              v-loading="loadingCacheList"
            >
              <ElTableColumn type="index" label="序号" width="60" />
              <ElTableColumn prop="cacheName" label="缓存名称" />
              <ElTableColumn prop="remark" label="备注" />
              <ElTableColumn label="操作" width="80">
                <template #default="{ row }">
                  <ElButton
                    type="danger"
                    :icon="Delete"
                    circle
                    size="small"
                    @click="handleDeleteCache(row)"
                    :loading="deletingCache === row.cacheName"
                  />
                </template>
              </ElTableColumn>
            </ElTable>
          </ElCard>
        </div>

        <!-- 中间：键名列表（虚拟滚动，适用于大量键） -->
        <div class="key-list-panel">
          <ElCard shadow="hover" class="panel-card">
            <template #header>
              <div class="panel-header">
                <span>键名列表</span>
                <ElButton
                  :icon="Refresh"
                  circle
                  size="small"
                  @click="refreshKeys"
                  :loading="loadingKeys"
                />
              </div>
            </template>
            <div v-loading="loadingKeys" class="key-list-wrapper">
              <!-- 表头 -->
              <div class="key-list-header">
                <span class="col-index">序号</span>
                <span class="col-key">缓存键名</span>
                <span class="col-action">操作</span>
              </div>
              <!-- 虚拟滚动列表 -->
              <ArtVirtualList
                ref="keyListRef"
                :data="keyList"
                :item-height="48"
                :height="'100%'"
                item-key="key"
                class="key-list-virtual"
              >
                <template #default="{ item, index }">
                  <div
                    class="key-list-row"
                    :class="{ 'is-active': selectedKey?.key === resolveCacheKeyInfo(item).key }"
                    @click="handleKeySelect(resolveCacheKeyInfo(item))"
                  >
                    <span class="col-index">{{ index + 1 }}</span>
                    <ElTooltip :content="resolveCacheKeyInfo(item).key" placement="top">
                      <span class="col-key text-ellipsis">{{ resolveCacheKeyInfo(item).key }}</span>
                    </ElTooltip>
                    <span class="col-action">
                      <ElButton
                        type="danger"
                        :icon="Delete"
                        circle
                        size="small"
                        @click.stop="handleDeleteKey(resolveCacheKeyInfo(item))"
                        :loading="deletingKey === resolveCacheKeyInfo(item).key"
                      />
                    </span>
                  </div>
                </template>
              </ArtVirtualList>
            </div>
          </ElCard>
        </div>

        <!-- 右侧：缓存内容 -->
        <div class="cache-content-panel">
          <ElCard shadow="hover" class="panel-card">
            <template #header>
              <div class="panel-header">
                <span>缓存内容</span>
                <div>
                  <ElButton
                    type="danger"
                    size="small"
                    @click="handleClearAll"
                    :loading="clearingAll"
                  >
                    清理全部
                  </ElButton>
                  <ElButton
                    :icon="Refresh"
                    circle
                    size="small"
                    @click="refreshContent"
                    :loading="loadingContent"
                  />
                </div>
              </div>
            </template>
            <ElForm :model="cacheContent" label-width="100px">
              <ElFormItem label="缓存名称">
                <ElInput v-model="cacheContent.cacheName" disabled />
              </ElFormItem>
              <ElFormItem label="缓存键名">
                <ElInput v-model="cacheContent.key" disabled />
              </ElFormItem>
              <ElFormItem label="缓存内容">
                <ElInput v-model="cacheContent.content" type="textarea" :rows="15" disabled />
              </ElFormItem>
            </ElForm>
          </ElCard>
        </div>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import {Delete, Refresh} from '@element-plus/icons-vue'
import {
  fetchClearAllCache,
  fetchDeleteCache,
  fetchDeleteCacheKey,
  fetchGetCacheContent,
  fetchGetCacheKeys,
  fetchGetCacheList
} from '@/api/monitor/cache'
import type {CacheContentInfo, CacheKeyInfo, RedisCacheInfo} from '@/types/api/monitor'
import {ElMessage, ElMessageBox} from 'element-plus'
import ArtVirtualList from '@/components/core/lists/art-virtual-list/index.vue'

defineOptions({ name: 'CacheManage' })

  const loading = ref(false)
  const keyListRef = ref<InstanceType<typeof ArtVirtualList> | null>(null)
  const cacheTableRef = ref<{
    setCurrentRow: (row?: RedisCacheInfo) => void
  } | null>(null)

  const loadingCacheList = ref(false)
  const loadingKeys = ref(false)
  const loadingContent = ref(false)
  const deletingCache = ref<string | null>(null)
  const deletingKey = ref<string | null>(null)
  const clearingAll = ref(false)

  const cacheList = ref<RedisCacheInfo[]>([])
  const selectedCache = ref<RedisCacheInfo | null>(null)
  const keyList = ref<CacheKeyInfo[]>([])
  const selectedKey = ref<CacheKeyInfo | null>(null)

  const resolveCacheKeyInfo = (item: Record<string, unknown>): CacheKeyInfo =>
    item as unknown as CacheKeyInfo
  const cacheContent = ref<CacheContentInfo>({
    cacheName: '',
    key: '',
    content: '',
    type: '',
    ttl: -2
  })

  /**
   * 获取缓存列表
   */
  /** 列表刷新后，若当前选中的分组已不存在（例如已删空），清除高亮与中间栏 */
  const syncSelectionWithCacheList = () => {
    const name = selectedCache.value?.cacheName
    if (!name) {
      return
    }
    const exists = cacheList.value.some((c) => c.cacheName === name)
    if (!exists) {
      cacheTableRef.value?.setCurrentRow(undefined)
      selectedCache.value = null
      selectedKey.value = null
      keyList.value = []
      cacheContent.value = {
        cacheName: '',
        key: '',
        content: '',
        type: '',
        ttl: -2
      }
    }
  }

  const getCacheList = async () => {
    loadingCacheList.value = true
    try {
      const data = await fetchGetCacheList()
      cacheList.value = data ?? []
      syncSelectionWithCacheList()
    } catch (error) {
      console.error('获取缓存列表失败:', error)
    } finally {
      loadingCacheList.value = false
    }
  }

  /**
   * 刷新缓存列表
   */
  const refreshCacheList = () => {
    getCacheList()
  }

  /**
   * 处理缓存选择
   */
  const handleCacheSelect = async (cache: RedisCacheInfo | null) => {
    selectedCache.value = cache
    selectedKey.value = null
    if (cache) {
      await getCacheKeys(cache.cacheName)
      cacheContent.value = {
        cacheName: cache.cacheName,
        key: '',
        content: '',
        type: '',
        ttl: -2
      }
    } else {
      keyList.value = []
      cacheContent.value = {
        cacheName: '',
        key: '',
        content: '',
        type: '',
        ttl: -2
      }
    }
  }

  /**
   * 获取键名列表
   */
  const getCacheKeys = async (cacheName: string) => {
    loadingKeys.value = true
    try {
      const data = await fetchGetCacheKeys(cacheName)
      keyList.value = data
      nextTick(() => keyListRef.value?.scrollToTop())
    } catch (error) {
      console.error('获取键名列表失败:', error)
      ElMessage.error('获取键名列表失败')
    } finally {
      loadingKeys.value = false
    }
  }

  /**
   * 刷新键名列表
   */
  const refreshKeys = () => {
    if (selectedCache.value) {
      getCacheKeys(selectedCache.value.cacheName)
    }
  }

  /**
   * 处理键选择
   */
  const handleKeySelect = async (key: CacheKeyInfo | null) => {
    selectedKey.value = key
    if (key && selectedCache.value) {
      await getCacheContent(selectedCache.value.cacheName, key.key)
    } else {
      cacheContent.value = {
        cacheName: selectedCache.value?.cacheName || '',
        key: '',
        content: '',
        type: '',
        ttl: -2
      }
    }
  }

  /**
   * 获取缓存内容
   */
  const getCacheContent = async (cacheName: string, key: string) => {
    loadingContent.value = true
    try {
      const data = await fetchGetCacheContent(cacheName, key)
      cacheContent.value = data
    } catch (error) {
      console.error('获取缓存内容失败:', error)
      ElMessage.error('获取缓存内容失败')
    } finally {
      loadingContent.value = false
    }
  }

  /**
   * 刷新缓存内容
   */
  const refreshContent = () => {
    if (selectedCache.value && selectedKey.value) {
      getCacheContent(selectedCache.value.cacheName, selectedKey.value.key)
    }
  }

  /**
   * 删除缓存
   */
  const handleDeleteCache = async (cache: RedisCacheInfo) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除缓存 "${cache.cacheName}" 的所有键吗？此操作不可恢复！`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      deletingCache.value = cache.cacheName
      try {
        const deletedCount = await fetchDeleteCache(cache.cacheName)
        if (deletedCount > 0) {
          ElMessage.success(`删除成功，共删除 ${deletedCount} 个键`)
        } else {
          ElMessage.warning('未匹配到可删除的键（可能已被删除或键格式与分组不一致）')
        }
        await getCacheList()
        if (selectedCache.value?.cacheName === cache.cacheName) {
          await getCacheKeys(cache.cacheName)
        }
      } catch (error) {
        console.error('删除缓存失败:', error)
        ElMessage.error('删除缓存失败')
      } finally {
        deletingCache.value = null
      }
    } catch {
      // 用户取消
    }
  }

  /**
   * 删除键
   */
  const handleDeleteKey = async (key: CacheKeyInfo) => {
    if (!selectedCache.value) {
      return
    }

    try {
      await ElMessageBox.confirm(`确定要删除键 "${key.key}" 吗？此操作不可恢复！`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      deletingKey.value = key.key
      try {
        await fetchDeleteCacheKey(selectedCache.value.cacheName, key.key)
        ElMessage.success('删除成功')
        await getCacheKeys(selectedCache.value.cacheName)
        if (keyList.value.length === 0) {
          await getCacheList()
        }
        cacheContent.value = {
          cacheName: selectedCache.value?.cacheName || '',
          key: '',
          content: '',
          type: '',
          ttl: -2
        }
      } catch (error) {
        console.error('删除键失败:', error)
        ElMessage.error('删除键失败')
      } finally {
        deletingKey.value = null
      }
    } catch {
      // 用户取消
    }
  }

  /**
   * 清空所有缓存
   */
  const handleClearAll = async () => {
    try {
      await ElMessageBox.confirm('确定要清空所有缓存吗？此操作不可恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      clearingAll.value = true
      try {
        await fetchClearAllCache()
        ElMessage.success('清空成功')
        cacheTableRef.value?.setCurrentRow(undefined)
        selectedCache.value = null
        selectedKey.value = null
        await getCacheList()
        keyList.value = []
        cacheContent.value = {
          cacheName: '',
          key: '',
          content: '',
          type: '',
          ttl: -2
        }
      } catch (error) {
        console.error('清空缓存失败:', error)
        ElMessage.error('清空缓存失败')
      } finally {
        clearingAll.value = false
      }
    } catch {
      // 用户取消
    }
  }

  /**
   * 处理刷新按钮点击
   */
  const handleRefresh = async () => {
    await getCacheList()
    if (selectedCache.value) {
      await getCacheKeys(selectedCache.value.cacheName)
    }
    if (selectedCache.value && selectedKey.value) {
      await getCacheContent(selectedCache.value.cacheName, selectedKey.value.key)
    }
  }

  onMounted(() => {
    getCacheList()
  })
</script>

<style scoped lang="scss">
  .redis-page {
    padding: 20px;
    background-color: var(--default-bg-color);
  }

  :deep(.art-table-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: 0 2px 12px 0 rgb(0 0 0 / 8%);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px 0 rgb(0 0 0 / 12%);
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .cache-container {
    display: flex;
    gap: 20px;
    height: calc(100vh - 300px);
  }

  .cache-list-panel,
  .cache-content-panel {
    display: flex;
    flex-direction: column;
    width: 400px;
  }

  .key-list-panel {
    display: flex;
    flex-direction: column;
    width: 500px;
  }

  .panel-card {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: 0 2px 8px 0 rgb(0 0 0 / 6%);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 12px 0 rgb(0 0 0 / 10%);
    }

    :deep(.el-card__header) {
      padding: 14px 18px;
      font-weight: 600;
      color: var(--art-gray-800);
      border-bottom: 1px solid var(--art-card-border);
    }

    :deep(.el-card__body) {
      display: flex;
      flex: 1;
      flex-direction: column;
      overflow: hidden;
    }
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  :deep(.el-table) {
    flex: 1;
    overflow: auto;
    border-radius: 8px;

    .el-table__header-wrapper {
      th {
        font-weight: 600;
        color: var(--art-gray-800);
        background-color: var(--art-gray-100) !important;
      }
    }

    .el-table__body-wrapper {
      tr {
        transition: all 0.2s ease;

        &:hover > td {
          background-color: var(--art-gray-50) !important;
        }
      }
    }
  }

  .key-list-wrapper {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-height: 0;
  }

  .key-list-header {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    padding: 10px 12px;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-regular);
    background: var(--el-fill-color-lighter);
    border-radius: 8px 8px 0 0;

    .col-index {
      flex-shrink: 0;
      width: 60px;
    }

    .col-key {
      flex: 1;
      min-width: 0;
    }

    .col-action {
      flex-shrink: 0;
      width: 80px;
      text-align: center;
    }
  }

  .key-list-virtual {
    flex: 1;
    min-height: 0;
  }

  .key-list-row {
    display: flex;
    align-items: center;
    padding: 0 12px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: var(--el-fill-color-light);
    }

    &.is-active {
      background: var(--el-color-primary-light-9);
    }

    .col-index {
      flex-shrink: 0;
      width: 60px;
    }

    .col-key {
      flex: 1;
      min-width: 0;
    }

    .col-action {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 80px;
    }
  }

  .text-ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  :deep(.el-form) {
    flex: 1;
    overflow: auto;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--art-gray-700);
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    transition: all 0.3s ease;
  }

  :deep(.el-button) {
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }
</style>
