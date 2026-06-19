<template>
  <div v-if="filePaths.length > 0" class="preview-layout">
    <aside class="file-panel">
      <div class="file-panel__title">文件路径</div>
      <ElScrollbar class="file-panel__scroll">
        <ElTree
          ref="treeRef"
          class="file-tree"
          :data="fileTree"
          node-key="id"
          default-expand-all
          highlight-current
          :expand-on-click-node="false"
          :props="{ label: 'label', children: 'children' }"
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <span
              :class="[
                'tree-node',
                data.isFile ? 'tree-node--file' : 'tree-node--folder',
                { 'is-active': data.fullPath === activeFile }
              ]"
              :title="data.isFile ? data.fullPath : node.label"
            >
              <ArtSvgIcon
                :icon="data.isFile ? 'ri:file-code-line' : 'ri:folder-3-line'"
                class="tree-node__icon"
              />
              <span class="tree-node__label">{{ node.label }}</span>
            </span>
          </template>
        </ElTree>
      </ElScrollbar>
    </aside>
    <section class="code-panel">
      <div class="code-panel__header">
        <span class="code-panel__path" :title="activeFile">{{ activeFile || '请选择文件' }}</span>
        <ElButton
          v-if="activeFile && activeCode"
          link
          type="primary"
          size="small"
          class="code-panel__copy"
          @click="copyCurrentFile"
        >
          复制
        </ElButton>
      </div>
      <ElScrollbar class="code-panel__scroll">
        <pre class="code-block">{{ activeCode }}</pre>
      </ElScrollbar>
    </section>
  </div>
  <ElEmpty v-else description="暂无预览代码" class="empty-tip" />
</template>

<script setup lang="ts">
  import type { TreeInstance } from 'element-plus'
  import { ElButton, ElEmpty, ElMessage, ElScrollbar, ElTree } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import {
    buildPreviewPathTree,
    type PreviewTreeNode
  } from '@/utils/generator/build-preview-path-tree'

  const props = defineProps<{
    previewCodeMap: Record<string, string>
  }>()

  const activeFile = ref('')
  const treeRef = ref<TreeInstance>()

  const filePaths = computed(() => Object.keys(props.previewCodeMap))

  const fileTree = computed(() => buildPreviewPathTree(filePaths.value))

  const activeCode = computed(() => {
    if (!activeFile.value) return ''
    return props.previewCodeMap[activeFile.value] ?? ''
  })

  function handleNodeClick(data: PreviewTreeNode) {
    if (data.isFile && data.fullPath) {
      activeFile.value = data.fullPath
      treeRef.value?.setCurrentKey(data.id)
    }
  }

  function syncTreeCurrentKey() {
    nextTick(() => {
      if (activeFile.value) {
        treeRef.value?.setCurrentKey(activeFile.value)
      }
    })
  }

  function reset() {
    activeFile.value = ''
  }

  function selectFirstFile() {
    const keys = Object.keys(props.previewCodeMap)
    activeFile.value = keys[0] ?? ''
    syncTreeCurrentKey()
  }

  async function copyText(text: string, emptyTip = '暂无内容可复制') {
    if (!text) {
      ElMessage.warning(emptyTip)
      return false
    }
    try {
      await navigator.clipboard.writeText(text)
      ElMessage.success('已复制到剪贴板')
      return true
    } catch {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        ElMessage.success('已复制到剪贴板')
        return true
      } catch {
        ElMessage.error('复制失败，请手动选择复制')
        return false
      } finally {
        document.body.removeChild(textarea)
      }
    }
  }

  async function copyCurrentFile() {
    await copyText(activeCode.value)
  }

  async function copyAllFiles() {
    const entries = Object.entries(props.previewCodeMap)
    if (!entries.length) {
      ElMessage.warning('暂无内容可复制')
      return false
    }
    const text = entries
      .map(([path, content]) => `/* ===== ${path} ===== */\n${content}`)
      .join('\n\n')
    return copyText(text)
  }

  watch(
    () => props.previewCodeMap,
    () => selectFirstFile(),
    { deep: true, immediate: true }
  )

  watch(activeFile, () => syncTreeCurrentKey())

  defineExpose({ reset, selectFirstFile, copyCurrentFile, copyAllFiles })
</script>

<style scoped lang="scss">
  .preview-layout {
    display: flex;
    gap: 0;
    height: 100%;
    overflow: hidden;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
  }

  .file-panel {
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    width: 360px;
    background: var(--el-fill-color-lighter);
    border-right: 1px solid var(--el-border-color-lighter);

    &__title {
      flex-shrink: 0;
      padding: 10px 12px;
      font-size: 13px;
      font-weight: 600;
      color: var(--el-text-color-secondary);
      border-bottom: 1px solid var(--el-border-color-lighter);
    }

    &__scroll {
      flex: 1;
      min-height: 0;
    }
  }

  .file-tree {
    padding: 8px 6px;
    background: transparent;

    :deep(.el-tree-node__content) {
      height: 32px;
      border-radius: 6px;
    }

    :deep(.el-tree-node.is-current > .el-tree-node__content) {
      background: transparent;
    }
  }

  .tree-node {
    display: inline-flex;
    gap: 6px;
    align-items: center;
    max-width: calc(100% - 4px);
    font-size: 13px;
    color: var(--el-text-color-regular);

    &--file {
      cursor: pointer;
    }

    &--folder {
      font-weight: 500;
      color: var(--el-text-color-primary);
    }

    &.is-active {
      color: var(--el-color-primary);
    }

    &__icon {
      flex-shrink: 0;
      font-size: 15px;
    }

    &__label {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &.is-active &__icon,
    &.is-active &__label {
      color: var(--el-color-primary);
    }
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content .tree-node--file) {
    background: var(--el-color-primary-light-9);
    border-radius: 4px;
  }

  .code-panel {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
    background: #1e1e1e;

    &__header {
      display: flex;
      flex-shrink: 0;
      gap: 8px;
      align-items: center;
      justify-content: space-between;
      padding: 8px 12px;
      background: #252526;
      border-bottom: 1px solid #3c3c3c;
    }

    &__path {
      flex: 1;
      min-width: 0;
      overflow: hidden;
      font-family: Menlo, Monaco, Consolas, monospace;
      font-size: 12px;
      color: #9cdcfe;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__copy {
      flex-shrink: 0;
      color: #9cdcfe !important;

      &:hover {
        color: #fff !important;
      }
    }

    &__scroll {
      flex: 1;
      min-height: 0;
    }
  }

  .code-block {
    min-height: 100%;
    padding: 12px 16px;
    margin: 0;
    font-family: Menlo, Monaco, Consolas, 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.55;
    color: #dcdcdc;
    white-space: pre;
    background: #1e1e1e;
  }

  .empty-tip {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 400px;
  }
</style>
