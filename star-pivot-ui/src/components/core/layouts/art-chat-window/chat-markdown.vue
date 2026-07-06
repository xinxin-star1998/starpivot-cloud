<template>
  <div class="chat-markdown-wrap">
    <div v-highlight class="chat-markdown" v-html="html" />
    <span v-if="streaming" class="chat-markdown-cursor" aria-hidden="true" />
  </div>
</template>

<script setup lang="ts">
import { renderChatMarkdown } from '@/utils/ai/render-markdown'

const props = withDefaults(
  defineProps<{
    content: string
    streaming?: boolean
  }>(),
  {
    streaming: false
  }
)

const html = computed(() => renderChatMarkdown(props.content, props.streaming))
</script>

<style scoped lang="scss">
.chat-markdown-wrap {
  display: block;
  min-width: 0;
  width: 100%;
}

.chat-markdown {
  font-size: 13px;
  line-height: 1.65;
  word-break: break-word;
  color: inherit;

  :deep(p) {
    margin: 0 0 0.65em;

    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(ul),
  :deep(ol) {
    margin: 0.35em 0 0.65em;
    padding-left: 1.35em;
  }

  :deep(li) {
    margin: 0.2em 0;
  }

  :deep(h1) {
    margin: 0.75em 0 0.4em;
    font-size: 1.15em;
    font-weight: 700;
    line-height: 1.4;
  }

  :deep(h2) {
    margin: 0.7em 0 0.35em;
    font-size: 1.08em;
    font-weight: 700;
    line-height: 1.4;
  }

  :deep(h3),
  :deep(h4) {
    margin: 0.6em 0 0.3em;
    font-size: 1em;
    font-weight: 600;
    line-height: 1.4;
  }

  :deep(h1:first-child),
  :deep(h2:first-child),
  :deep(h3:first-child) {
    margin-top: 0;
  }

  :deep(hr) {
    margin: 0.75em 0;
    border: none;
    border-top: 1px solid rgb(0 0 0 / 10%);
  }

  :deep(strong) {
    font-weight: 600;
  }

  :deep(code:not(pre code)) {
    padding: 0.12em 0.4em;
    border-radius: 4px;
    background: rgb(0 0 0 / 7%);
    font-family: Consolas, Monaco, 'Courier New', monospace;
    font-size: 0.9em;
  }

  :deep(pre) {
    position: relative;
    margin: 0.65em 0;
    padding: 0.75em 1em;
    overflow-x: auto;
    border-radius: 8px;
    background: #282c34;
    color: #abb2bf;
    font-size: 12px;
    line-height: 1.55;
  }

  :deep(pre code) {
    display: block;
    padding: 0;
    overflow-x: auto;
    background: transparent;
    color: inherit;
    font-family: Consolas, Monaco, 'Courier New', monospace;
    font-size: inherit;
    white-space: pre;
    word-break: normal;
  }

  :deep(blockquote) {
    margin: 0.5em 0;
    padding: 0.35em 0 0.35em 0.75em;
    border-left: 3px solid var(--el-color-primary);
    color: rgb(0 0 0 / 65%);
  }

  :deep(a) {
    color: var(--el-color-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(table) {
    display: block;
    max-width: 100%;
    margin: 0.5em 0;
    overflow-x: auto;
    border-collapse: collapse;
    font-size: 12px;
  }

  :deep(th),
  :deep(td) {
    padding: 0.4em 0.55em;
    border: 1px solid rgb(0 0 0 / 10%);
    text-align: left;
  }

  :deep(th) {
    background: rgb(0 0 0 / 4%);
    font-weight: 600;
  }
}

.chat-markdown-cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  margin-left: 2px;
  vertical-align: text-bottom;
  background: currentcolor;
  animation: chat-markdown-blink 1s step-end infinite;
}

@keyframes chat-markdown-blink {
  50% {
    opacity: 0;
  }
}
</style>
