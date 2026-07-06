<!-- WangEditor 富文本编辑器 插件地址：https://www.wangeditor.com/ -->
<template>
  <div class="editor-wrapper">
    <Toolbar
      class="editor-toolbar"
      :editor="editorRef"
      :mode="mode"
      :defaultConfig="toolbarConfig"
    />
    <Editor
      :style="{ height: height, overflowY: 'hidden' }"
      v-model="editorHtml"
      :mode="mode"
      :defaultConfig="editorConfig"
      @onCreated="onCreateEditor"
    />
  </div>
</template>

<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import {useUserStore} from '@/store/modules/user'
import {getApiBaseUrl} from '@/utils/http'
import {buildAbsoluteApiUrl} from '@/utils/http/api-path'
import EmojiText from '@/utils/ui/emojo'
import {
  normalizeEditorHtmlForStorage,
  resolveEditorHtmlForDisplay
} from '@/utils/storage/editor-oss-image'
import {IDomEditor, IEditorConfig, IToolbarConfig} from '@wangeditor/editor'

defineOptions({ name: 'ArtWangEditor' })

  // Props 定义
  interface Props {
    /** 编辑器高度 */
    height?: string
    /** 自定义工具栏配置 */
    toolbarKeys?: string[]
    /** 插入新工具到指定位置 */
    insertKeys?: { index: number; keys: string[] }
    /** 排除的工具栏项 */
    excludeKeys?: string[]
    /** 编辑器模式 */
    mode?: 'default' | 'simple'
    /** 占位符文本 */
    placeholder?: string
    /** 上传配置 */
    uploadConfig?: {
      maxFileSize?: number
      maxNumberOfFiles?: number
      server?: string
    }
  }

  const props = withDefaults(defineProps<Props>(), {
    height: '500px',
    mode: 'default',
    placeholder: '请输入内容...',
    excludeKeys: () => ['fontFamily']
  })

  const modelValue = defineModel<string>({ required: true })

  const editorHtml = ref('')
  let syncingFromParent = false
  let syncingToParent = false

  watch(
    () => modelValue.value,
    async (html) => {
      if (syncingToParent) {
        return
      }
      syncingFromParent = true
      try {
        editorHtml.value = await resolveEditorHtmlForDisplay(html || '')
      } finally {
        syncingFromParent = false
      }
    },
    { immediate: true }
  )

  watch(editorHtml, (html) => {
    if (syncingFromParent) {
      return
    }
    syncingToParent = true
    try {
      modelValue.value = normalizeEditorHtmlForStorage(html || '')
    } finally {
      syncingToParent = false
    }
  })

  // 编辑器实例
  const editorRef = shallowRef<IDomEditor>()
  const userStore = useUserStore()

  // 常量配置
  const DEFAULT_UPLOAD_CONFIG = {
    maxFileSize: 3 * 1024 * 1024, // 3MB
    maxNumberOfFiles: 10,
    fieldName: 'file',
    allowedFileTypes: ['image/*']
  } as const

  const uploadServer = computed(() => {
    if (props.uploadConfig?.server) return props.uploadConfig.server
    return buildAbsoluteApiUrl('/common/upload/wangeditor', getApiBaseUrl())
  })

  // 合并上传配置
  const mergedUploadConfig = computed(() => ({
    ...DEFAULT_UPLOAD_CONFIG,
    ...props.uploadConfig
  }))

  // 工具栏配置
  const toolbarConfig = computed((): Partial<IToolbarConfig> => {
    const config: Partial<IToolbarConfig> = {}

    // 完全自定义工具栏
    if (props.toolbarKeys && props.toolbarKeys.length > 0) {
      config.toolbarKeys = props.toolbarKeys
    }

    // 插入新工具
    if (props.insertKeys) {
      config.insertKeys = props.insertKeys
    }

    // 排除工具
    if (props.excludeKeys && props.excludeKeys.length > 0) {
      config.excludeKeys = props.excludeKeys
    }

    return config
  })

  const authHeader = computed(() => {
    const t = userStore.accessToken
    if (!t) return {}
    return {
      Authorization: t.startsWith('Bearer ') ? t : `Bearer ${t}`
    }
  })

  // 编辑器配置（computed：上传地址、Token 与运行时 config.js 保持一致）
  const editorConfig = computed(
    (): Partial<IEditorConfig> => ({
      placeholder: props.placeholder,
      MENU_CONF: {
        uploadImage: {
          fieldName: mergedUploadConfig.value.fieldName,
          maxFileSize: mergedUploadConfig.value.maxFileSize,
          maxNumberOfFiles: mergedUploadConfig.value.maxNumberOfFiles,
          allowedFileTypes: mergedUploadConfig.value.allowedFileTypes,
          server: uploadServer.value,
          headers: authHeader.value,
          customInsert(res: Record<string, unknown>, insertFn: (url: string, alt: string, href: string) => void) {
            const payload = (res?.data ?? res) as { url?: string; displayUrl?: string }
            const displayUrl = payload?.displayUrl || payload?.url || ''
            if (!displayUrl) {
              ElMessage.error(`图片上传失败 ${EmojiText[500]}`)
              return
            }
            insertFn(displayUrl, '', '')
          },
          onSuccess() {
            ElMessage.success(`图片上传成功 ${EmojiText[200]}`)
          },
          onError(file: File, err: any, res: any) {
            console.error('图片上传失败:', err, res)
            ElMessage.error(`图片上传失败 ${EmojiText[500]}`)
          }
        }
      }
    })
  )

  // 编辑器创建回调
  const onCreateEditor = (editor: IDomEditor) => {
    editorRef.value = editor

    // 监听全屏事件
    editor.on('fullScreen', () => {
      // 编辑器进入全屏模式
    })

    // 确保在编辑器创建后应用自定义图标
    applyCustomIcons()
  }

  // 应用自定义图标（带重试机制）
  const applyCustomIcons = () => {
    let retryCount = 0
    const maxRetries = 10
    const retryDelay = 100

    const tryApplyIcons = () => {
      const editor = editorRef.value
      if (!editor) {
        if (retryCount < maxRetries) {
          retryCount++
          setTimeout(tryApplyIcons, retryDelay)
        }
        return
      }

      // 获取当前编辑器的工具栏容器
      const editorContainer = editor.getEditableContainer().closest('.editor-wrapper')
      if (!editorContainer) {
        if (retryCount < maxRetries) {
          retryCount++
          setTimeout(tryApplyIcons, retryDelay)
        }
        return
      }

      const toolbar = editorContainer.querySelector('.w-e-toolbar')
      const toolbarButtons = editorContainer.querySelectorAll('.w-e-bar-item button[data-menu-key]')

      if (toolbar && toolbarButtons.length > 0) {
        return
      }

      // 如果工具栏还没渲染完成，继续重试
      if (retryCount < maxRetries) {
        retryCount++
        setTimeout(tryApplyIcons, retryDelay)
      } else {
        console.warn('工具栏渲染超时，无法应用自定义图标 - 编辑器实例:', editor.id)
      }
    }

    // 使用 requestAnimationFrame 确保在下一帧执行
    requestAnimationFrame(tryApplyIcons)
  }

  // 暴露编辑器实例和方法
  defineExpose({
    /** 获取编辑器实例 */
    getEditor: () => editorRef.value,
    /** 设置编辑器内容 */
    setHtml: (html: string) => editorRef.value?.setHtml(html),
    /** 获取编辑器内容 */
    getHtml: () => editorRef.value?.getHtml(),
    /** 清空编辑器 */
    clear: () => editorRef.value?.clear(),
    /** 聚焦编辑器 */
    focus: () => editorRef.value?.focus()
  })

  // 生命周期
  onMounted(() => {
    // 图标替换已在 onCreateEditor 中处理
  })

  onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor) {
      editor.destroy()
    }
  })
</script>

<style lang="scss">
  @use './style';
</style>
