<template>
  <div class="file-folder-tree">
    <div class="tree-header">
      <span class="tree-title">目录管理</span>
      <div class="tree-header-actions">
        <ElButton type="primary" link @click="emit('refresh')">刷新</ElButton>
        <ElButton
          v-if="hasAuth('file:folder:add')"
          type="primary"
          link
          @click="emit('add-folder', activeCategory || categories[0]?.category)"
        >
          <ArtSvgIcon icon="ri:add-line" class="mr-0.5" />
          新建
        </ElButton>
      </div>
    </div>

    <p class="tree-desc">目录只负责业务归类，不直接映射对象存储路径。</p>

    <div class="tree-stats">
      <div class="tree-stat-card">
        <span class="tree-stat-label">目录数</span>
        <span class="tree-stat-value">{{ folderCount }}</span>
      </div>
      <div class="tree-stat-card">
        <span class="tree-stat-label">当前目录</span>
        <span class="tree-stat-value" :title="currentDirLabel">{{ currentDirLabel }}</span>
      </div>
    </div>

    <ElInput
      v-model="filterText"
      clearable
      placeholder="搜索分类或文件夹"
      class="tree-filter"
      :prefix-icon="Search"
    />

    <ElScrollbar class="tree-scroll">
      <ElTree
        ref="treeRef"
        :data="filteredTreeData"
        node-key="nodeKey"
        :props="treeProps"
        highlight-current
        default-expand-all
        :expand-on-click-node="false"
        :current-node-key="currentNodeKey"
        @node-click="handleNodeClick"
      >
        <template #default="{ data }">
          <div
            class="tree-node"
            :class="{
              'is-folder': !!data.folderId,
              'is-all': data.isAll
            }"
          >
            <ArtSvgIcon
              :icon="
                data.isAll
                  ? 'ri:folder-open-line'
                  : data.folderId
                    ? 'ri:folder-3-line'
                    : getCategoryIcon(data.category)
              "
              class="node-icon"
            />
            <span class="node-label" :title="data.label">{{ data.label }}</span>
            <span v-if="data.fileCount != null" class="node-count">{{ data.fileCount }}</span>
            <ElDropdown
              v-if="data.folderId && data.folderName !== '默认' && hasFolderManage"
              trigger="click"
              @command="(cmd: string) => handleFolderCommand(cmd, data)"
            >
              <ElButton class="node-more" link :icon="MoreFilled" @click.stop />
              <template #dropdown>
                <ElDropdownMenu>
                  <ElDropdownItem v-if="hasAuth('file:folder:edit')" command="edit">
                    重命名
                  </ElDropdownItem>
                  <ElDropdownItem
                    v-if="hasAuth('file:folder:delete')"
                    command="delete"
                    divided
                  >
                    删除
                  </ElDropdownItem>
                </ElDropdownMenu>
              </template>
            </ElDropdown>
          </div>
        </template>
      </ElTree>
      <ElEmpty v-if="filteredTreeData.length === 0" :image-size="64" description="无匹配文件夹" />
    </ElScrollbar>
  </div>
</template>

<script setup lang="ts">
import type {FileCategoryNode} from '@/api/file/types'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import {useAuth} from '@/hooks/core/useAuth'
import {getCategoryIcon} from '../constants'
import {MoreFilled, Search} from '@element-plus/icons-vue'
import type {ElTree} from 'element-plus'
import {computed, nextTick, ref, watch} from 'vue'

defineOptions({ name: 'FileFolderTree' })

  const ALL_NODE_KEY = 'all-files'

  type TreeNode = {
    nodeKey: string
    label: string
    isAll?: boolean
    folderId?: number
    folderName?: string
    category?: string
    fileCount?: number
    children?: TreeNode[]
  }

  const props = defineProps<{
    categories: FileCategoryNode[]
    /** 选中「全部文件」时为 true */
    allSelected?: boolean
    activeFolderId?: number
  }>()

  const emit = defineEmits<{
    'select-all': []
    'select-folder': [payload: { folderId: number; category: string; folderName: string }]
    'add-folder': [category?: string]
    'edit-folder': [payload: { folderId: number; category: string; folderName: string }]
    'delete-folder': [folderId: number]
    refresh: []
  }>()

  const { hasAuth } = useAuth()
  const treeRef = ref<InstanceType<typeof ElTree>>()
  const filterText = ref('')

  const hasFolderManage = computed(
    () => hasAuth('file:folder:edit') || hasAuth('file:folder:delete')
  )

  const folderCount = computed(() =>
    props.categories.reduce((sum, cat) => sum + (cat.children?.length || 0), 0)
  )

  const totalFileCount = computed(() =>
    props.categories.reduce(
      (sum, cat) =>
        sum + (cat.children || []).reduce((s, f) => s + (f.fileCount ?? 0), 0),
      0
    )
  )

  const activeCategory = computed(() => {
    if (!props.activeFolderId) return ''
    for (const cat of props.categories) {
      if (cat.children?.some((f) => f.folderId === props.activeFolderId)) {
        return cat.category
      }
    }
    return ''
  })

  const currentDirLabel = computed(() => {
    if (props.allSelected || !props.activeFolderId) return '全部文件'
    for (const cat of props.categories) {
      const folder = cat.children?.find((f) => f.folderId === props.activeFolderId)
      if (folder) return folder.folderName || '默认'
    }
    return '全部文件'
  })

  const currentNodeKey = computed(() => {
    if (props.allSelected || !props.activeFolderId) return ALL_NODE_KEY
    return `folder-${props.activeFolderId}`
  })

  const treeData = computed<TreeNode[]>(() => [
    {
      nodeKey: ALL_NODE_KEY,
      label: '全部文件',
      isAll: true,
      fileCount: totalFileCount.value,
      children: props.categories.map((cat) => ({
        nodeKey: `cat-${cat.category}`,
        label: cat.categoryLabel,
        category: cat.category,
        children: (cat.children || []).map((folder) => ({
          nodeKey: `folder-${folder.folderId}`,
          label: folder.folderName || '',
          folderName: folder.folderName,
          folderId: folder.folderId,
          category: cat.category,
          fileCount: folder.fileCount
        }))
      }))
    }
  ])

  const filteredTreeData = computed(() => {
    const kw = filterText.value.trim().toLowerCase()
    if (!kw) return treeData.value

    const root = treeData.value[0]
    const matchedCats = (root.children || [])
      .map((cat) => {
        const catMatch = cat.label.toLowerCase().includes(kw)
        const children = (cat.children || []).filter(
          (f) => catMatch || f.label.toLowerCase().includes(kw)
        )
        if (catMatch || children.length) {
          return { ...cat, children: catMatch ? cat.children : children }
        }
        return null
      })
      .filter(Boolean) as TreeNode[]

    if (root.label.toLowerCase().includes(kw) || matchedCats.length) {
      return [{ ...root, children: matchedCats.length ? matchedCats : root.children }]
    }
    return []
  })

  const treeProps = {
    children: 'children',
    label: 'label'
  }

  watch(
    currentNodeKey,
    (key) => {
      nextTick(() => treeRef.value?.setCurrentKey(key))
    },
    { immediate: true }
  )

  function handleNodeClick(data: TreeNode) {
    if (data.isAll) {
      emit('select-all')
      return
    }
    if (data.folderId && data.category) {
      emit('select-folder', {
        folderId: data.folderId,
        category: data.category,
        folderName: data.folderName || data.label || ''
      })
    }
  }

  function handleFolderCommand(command: string, data: TreeNode) {
    if (!data.folderId || !data.category) return
    if (command === 'edit') {
      emit('edit-folder', {
        folderId: data.folderId,
        category: data.category,
        folderName: data.folderName || data.label || ''
      })
    } else if (command === 'delete') {
      emit('delete-folder', data.folderId)
    }
  }
</script>

<style scoped lang="scss">
  .file-folder-tree {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
  }

  .tree-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
  }

  .tree-header-actions {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .tree-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .tree-desc {
    margin: 0 0 12px;
    font-size: 12px;
    line-height: 1.5;
    color: var(--el-text-color-secondary);
  }

  .tree-stats {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
    margin-bottom: 12px;
  }

  .tree-stat-card {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 10px 12px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    background: var(--el-fill-color-blank);
  }

  .tree-stat-label {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .tree-stat-value {
    overflow: hidden;
    font-size: 16px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .tree-filter {
    margin-bottom: 10px;
  }

  .tree-scroll {
    flex: 1;
    min-height: 0;
  }

  .tree-node {
    display: flex;
    align-items: center;
    gap: 6px;
    flex: 1;
    min-width: 0;
    padding-right: 4px;

    &.is-folder:hover .node-more {
      opacity: 1;
    }
  }

  .node-icon {
    flex-shrink: 0;
    font-size: 15px;
    color: var(--el-color-primary);
  }

  .node-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
  }

  .node-count {
    flex-shrink: 0;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    border-radius: 9px;
    background: var(--el-fill-color);
    color: var(--el-text-color-secondary);
    font-size: 11px;
    line-height: 18px;
    text-align: center;
  }

  .node-more {
    opacity: 0;
    flex-shrink: 0;
    padding: 0 2px;
    transition: opacity 0.15s;
  }

  :deep(.el-tree-node__content) {
    height: 34px;
    border-radius: 6px;
  }

  :deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
    background: var(--el-color-primary-light-9);
  }
</style>
