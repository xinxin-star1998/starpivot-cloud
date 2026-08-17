<template>
  <ElDialog
    v-model="visible"
    title="选择写盘根目录（服务端）"
    width="640px"
    append-to-body
    @open="onOpen"
  >
    <ElAlert
      type="info"
      :closable="false"
      show-icon
      class="mb-3"
      title="写盘在服务端执行，此处浏览的是服务器磁盘目录（非浏览器本机）。若前后端同机开发，选中的即为项目路径。"
    />
    <div class="dir-toolbar">
      <ElBreadcrumb separator="/">
        <ElBreadcrumbItem v-if="!dirData?.current">
          <a href="#" @click.prevent="loadDirs('')">根目录</a>
        </ElBreadcrumbItem>
        <ElBreadcrumbItem v-else>
          <a href="#" @click.prevent="loadDirs('')">根目录</a>
        </ElBreadcrumbItem>
        <ElBreadcrumbItem v-if="dirData?.current">
          <span>{{ dirData.current }}</span>
        </ElBreadcrumbItem>
      </ElBreadcrumb>
      <ElButton v-if="dirData?.parent" link type="primary" @click="loadDirs(dirData.parent!)">
        上级目录
      </ElButton>
    </div>
    <div v-loading="loading" class="dir-list">
      <div
        v-for="item in dirData?.directories || []"
        :key="item.path"
        class="dir-item"
        @dblclick="loadDirs(item.path)"
      >
        <ArtSvgIcon icon="ri:folder-3-line" class="dir-item__icon" />
        <span class="dir-item__name" :title="item.path">{{ item.name }}</span>
        <ElButton link type="primary" size="small" @click.stop="loadDirs(item.path)">进入</ElButton>
        <ElButton link type="success" size="small" @click.stop="selectDir(item.path)"
          >选择</ElButton
        >
      </div>
      <ElEmpty v-if="!loading && !dirData?.directories?.length" description="暂无子目录" />
    </div>
    <div v-if="dirData?.current" class="current-select">
      <span>当前目录：</span>
      <code>{{ dirData.current }}</code>
      <ElButton type="primary" size="small" class="ml-2" @click="selectDir(dirData.current!)">
        选择当前目录
      </ElButton>
    </div>
    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import {
    ElAlert,
    ElBreadcrumb,
    ElBreadcrumbItem,
    ElButton,
    ElDialog,
    ElEmpty,
    ElMessage
  } from 'element-plus'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { type ExternalWriteDirList, fetchExternalWriteDirs } from '@/api/generator/gen-external'

  const visible = defineModel<boolean>('visible', { default: false })
  const emit = defineEmits<{ select: [string] }>()

  const loading = ref(false)
  const dirData = ref<ExternalWriteDirList | null>(null)

  async function loadDirs(path?: string) {
    loading.value = true
    try {
      dirData.value = await fetchExternalWriteDirs(path)
    } catch {
      dirData.value = null
    } finally {
      loading.value = false
    }
  }

  function selectDir(path: string) {
    emit('select', path)
    visible.value = false
    ElMessage.success('已选择写盘根目录')
  }

  function onOpen() {
    loadDirs('')
  }
</script>

<style scoped lang="scss">
  .dir-toolbar {
    display: flex;
    gap: 12px;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  .dir-list {
    min-height: 240px;
    max-height: 360px;
    overflow: auto;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
  }

  .dir-item {
    display: flex;
    gap: 8px;
    align-items: center;
    padding: 10px 12px;
    cursor: default;
    border-bottom: 1px solid var(--el-border-color-extra-light);

    &:hover {
      background: var(--el-fill-color-light);
    }

    &__icon {
      flex-shrink: 0;
      font-size: 18px;
      color: var(--el-color-warning);
    }

    &__name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .current-select {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    margin-top: 12px;
    font-size: 13px;

    code {
      word-break: break-all;
    }
  }
</style>
