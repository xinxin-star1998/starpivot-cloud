<template>
  <span class="external-action-btn-wrap">
    <ElTooltip
      v-if="tipContent"
      :content="tipContent"
      placement="top"
      :show-after="200"
    >
      <span class="external-action-btn-trigger">
        <ElButton v-bind="btnProps" class="external-action-btn" @click="emit('click', $event)">
          <slot />
        </ElButton>
      </span>
    </ElTooltip>
    <ElButton
      v-else
      v-bind="btnProps"
      class="external-action-btn"
      @click="emit('click', $event)"
    >
      <slot />
    </ElButton>
  </span>
</template>

<script setup lang="ts">
import type {ButtonProps} from 'element-plus'
import {ElButton, ElTooltip} from 'element-plus'

defineOptions({ inheritAttrs: false })

  const props = defineProps<{
    /** 简要说明（并入 Tooltip） */
    what?: string
    /** 用法与注意点（悬停显示） */
    usage?: string
  }>()

  const emit = defineEmits<{ click: [event: MouseEvent] }>()

  const attrs = useAttrs()
  const btnProps = computed(() => attrs as Partial<ButtonProps>)

  const tipContent = computed(() => {
    const parts = [props.what, props.usage].filter(Boolean)
    return parts.length ? parts.join('：') : ''
  })
</script>

<style scoped lang="scss">
  .external-action-btn-wrap {
    display: inline-block;
    vertical-align: middle;
  }

  .external-action-btn-trigger {
    display: inline-block;
  }

  .external-action-btn {
    vertical-align: middle;
  }
</style>
