<template>
  <div class="file-folder-tree">
    <div class="tree-header">
      <span class="tree-title">文件夹</span>
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
          <div class="tree-node" :class="{ 'is-folder': !!data.folderId }">
            <ArtSvgIcon
              :icon="data.folderId ? 'ri:folder-3-line' : getCategoryIcon(data.category)"
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

const props = defineProps<{
    categories: FileCategoryNode[]
    activeFolderId?: number
  }>()

  const emit = defineEmits<{
    'select-folder': [payload: { folderId: number; category: string; folderName: string }]
    'add-folder': [category?: string]
    'edit-folder': [payload: { folderId: number; category: string; folderName: string }]
    'delete-folder': [folderId: number]
  }>()

  const { hasAuth } = useAuth()
  const treeRef = ref<InstanceType<typeof ElTree>>()
  const filterText = ref('')

  const hasFolderManage = computed(
    () => hasAuth('file:folder:edit') || hasAuth('file:folder:delete')
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

  const currentNodeKey = computed(() =>
    props.activeFolderId ? `folder-${props.activeFolderId}` : undefined
  )

  const treeData = computed(() =>
    props.categories.map((cat) => ({
      nodeKey: `cat-${cat.category}`,
      label: cat.categoryLabel,
      category: cat.category,
      children: (cat.children || []).map((folder) => ({
        nodeKey: `folder-${folder.folderId}`,
        label: folder.folderName,
        folderName: folder.folderName,
        folderId: folder.folderId,
        category: cat.category,
        fileCount: folder.fileCount
      }))
    }))
  )

  const filteredTreeData = computed(() => {
    const kw = filterText.value.trim().toLowerCase()
    if (!kw) return treeData.value
    return treeData.value
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
      .filter(Boolean) as typeof treeData.value
  })

  const treeProps = {
    children: 'children',
    label: 'label'
  }

  watch(
    () => props.activeFolderId,
    (id) => {
      if (id) {
        nextTick(() => treeRef.value?.setCurrentKey(`folder-${id}`))
      }
    },
    { immediate: true }
  )

  function handleNodeClick(data: {
    folderId?: number
    category?: string
    folderName?: string
    label?: string
  }) {
    if (data.folderId && data.category) {
      emit('select-folder', {
        folderId: data.folderId,
        category: data.category,
        folderName: data.folderName || data.label || ''
      })
    }
  }

  function handleFolderCommand(
    command: string,
    data: { folderId: number; category: string; folderName: string }
  ) {
    if (command === 'edit') {
      emit('edit-folder', data)
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
    margin-bottom: 10px;
  }

  .tree-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
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
