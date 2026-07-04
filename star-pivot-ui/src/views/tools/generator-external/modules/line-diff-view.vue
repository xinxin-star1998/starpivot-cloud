<template>
  <ElScrollbar class="line-diff-view">
    <div class="line-diff-table">
      <div
        v-for="(line, index) in lines"
        :key="index"
        :class="['line-diff-row', `line-diff-row--${line.type}`]"
      >
        <span class="line-diff-gutter line-diff-gutter--old">{{ line.oldLineNum ?? '' }}</span>
        <span class="line-diff-gutter line-diff-gutter--new">{{ line.newLineNum ?? '' }}</span>
        <span class="line-diff-marker">{{ marker(line.type) }}</span>
        <span class="line-diff-content">{{ line.content || ' ' }}</span>
      </div>
    </div>
  </ElScrollbar>
</template>

<script setup lang="ts">
import {ElScrollbar} from 'element-plus'
import {type DiffLine, diffLines, type DiffLineType} from '@/utils/text/line-diff'

const props = defineProps<{
    oldText?: string | null
    newText?: string | null
    mode?: 'unified' | 'old' | 'new'
  }>()

  const lines = computed<DiffLine[]>(() => {
    const oldText = props.oldText ?? ''
    const newText = props.newText ?? ''
    if (props.mode === 'old') {
      return diffLines(oldText, oldText)
    }
    if (props.mode === 'new') {
      return diffLines('', newText)
    }
    return diffLines(oldText, newText)
  })

  function marker(type: DiffLineType) {
    if (type === 'insert') return '+'
    if (type === 'delete') return '-'
    return ' '
  }
</script>

<style scoped lang="scss">
  .line-diff-view {
    height: 100%;
  }

  .line-diff-table {
    min-width: 100%;
    font-family: Menlo, Monaco, Consolas, 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.55;
    background: #1e1e1e;
  }

  .line-diff-row {
    display: flex;
    white-space: pre;
    border-left: 3px solid transparent;

    &--equal {
      color: #dcdcdc;
    }

    &--insert {
      color: #b5f2b5;
      background: rgb(46 160 67 / 18%);
      border-left-color: #3fb950;
    }

    &--delete {
      color: #ffb4b4;
      background: rgb(248 81 73 / 18%);
      border-left-color: #f85149;
    }
  }

  .line-diff-gutter {
    flex-shrink: 0;
    width: 44px;
    padding: 0 6px;
    color: #6e7681;
    text-align: right;
    user-select: none;
    background: #252526;
    border-right: 1px solid #3c3c3c;

    &--new {
      border-right: none;
    }
  }

  .line-diff-marker {
    flex-shrink: 0;
    width: 18px;
    padding-left: 4px;
    color: #8b949e;
    user-select: none;
  }

  .line-diff-content {
    flex: 1;
    padding-right: 12px;
  }
</style>
