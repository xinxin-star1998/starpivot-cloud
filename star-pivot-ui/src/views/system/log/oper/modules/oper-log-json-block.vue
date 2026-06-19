<!-- 操作日志 JSON/文本块（请求参数、返回结果） -->
<template>
  <div class="log-item param-item">
    <span class="log-label">{{ label }}</span>
    <div class="log-content">
      <div class="code-wrapper" :class="{ 'code-expanded': expanded }">
        <div class="code-header">
          <span class="code-label">{{ label }}</span>
          <div class="code-actions">
            <ElButton
              v-if="content && content !== '参数解析失败'"
              size="small"
              text
              class="copy-btn"
              @click="handleCopy"
            >
              <ElIcon><CopyDocument /></ElIcon>
              {{ copySuccess ? '已复制' : '复制' }}
            </ElButton>
            <ElButton
              v-if="isTooLong"
              size="small"
              text
              class="expand-btn"
              @click="expanded = !expanded"
            >
              <ElIcon>
                <ArrowUpBold v-if="expanded" />
                <ArrowDownBold v-else />
              </ElIcon>
              {{ expanded ? '收起' : '展开' }}
            </ElButton>
          </div>
        </div>
        <pre
          class="code-block"
          :class="[resultStyle ? 'result-block' : '', { 'code-collapsed': isTooLong && !expanded }]"
          >{{ formattedContent }}</pre
        >
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { ElButton, ElIcon } from 'element-plus'
  import { CopyDocument, ArrowDownBold, ArrowUpBold } from '@element-plus/icons-vue'
  import { formatOperLogParam } from './oper-log-utils'

  interface Props {
    label: string
    content?: string
    /** 超过该长度显示展开/收起 */
    collapseThreshold?: number
    /** 使用返回结果配色 */
    resultStyle?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    collapseThreshold: 1000,
    resultStyle: false
  })

  const expanded = ref(false)
  const copySuccess = ref(false)

  const formattedContent = computed(() => formatOperLogParam(props.content))
  const isTooLong = computed(() => (props.content?.length ?? 0) > props.collapseThreshold)

  const handleCopy = async () => {
    if (!props.content) return
    try {
      await navigator.clipboard.writeText(formattedContent.value)
      copySuccess.value = true
      setTimeout(() => (copySuccess.value = false), 2000)
    } catch (err) {
      console.error('复制失败:', err)
    }
  }
</script>

<style scoped lang="scss">
  @use './oper-log-detail.scss';
</style>
