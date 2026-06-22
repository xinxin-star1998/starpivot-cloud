<!-- 表格按钮 -->
<template>
  <div
    :class="[
      'inline-flex items-center justify-center min-w-6 h-6 px-1.5 mr-1.5 text-xs c-p rounded align-middle',
      buttonClass
    ]"
    :style="{ backgroundColor: buttonBgColor, color: iconColor }"
    :title="tooltipText"
    @click="handleClick"
  >
    <ArtSvgIcon :icon="iconContent" class="text-[0.875rem]" />
    <span v-if="label" class="ml-0.5">{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'ArtButtonTable' })

  interface Props {
    /** 按钮类型 */
    type?:
      | 'add'
      | 'edit'
      | 'delete'
      | 'more'
      | 'view'
      | 'sync'
      | 'download'
      | 'generate'
      | 'execute'
      | 'pause'
      | 'resume'
    /** 按钮图标 */
    icon?: string
    /** 按钮样式类 */
    iconClass?: string
    /** icon 颜色 */
    iconColor?: string
    /** 按钮背景色 */
    buttonBgColor?: string
    /** 按钮文本标签 */
    label?: string
    /** 鼠标悬浮提示 */
    tooltip?: string
  }

  const props = withDefaults(defineProps<Props>(), {})

  const emit = defineEmits<{
    (e: 'click'): void
  }>()

  // 默认按钮配置
  const defaultButtons: Record<NonNullable<Props['type']>, { icon: string; class: string }> = {
    add: { icon: 'ri:add-fill', class: 'bg-theme/12 text-theme' },
    edit: { icon: 'ri:pencil-line', class: 'bg-secondary/12 text-secondary' },
    delete: { icon: 'ri:delete-bin-5-line', class: 'bg-error/12 text-error' },
    view: { icon: 'ri:eye-line', class: 'bg-info/12 text-info' },
    sync: { icon: 'ri:refresh-line', class: 'bg-info/12 text-info' },
    download: { icon: 'ri:download-line', class: 'bg-info/12 text-info' },
    generate: { icon: 'ri:code-s-slash-line', class: 'bg-success/12 text-success' },
    execute: { icon: 'ri:play-circle-line', class: 'bg-success/12 text-success' },
    // 新增：暂停按钮（黄色系 + 暂停图标）
    pause: { icon: 'ri:pause-circle-line', class: 'bg-warning/12 text-warning' },
    // 新增：恢复按钮（蓝色系 + 播放图标）
    resume: { icon: 'ri:play-circle-fill', class: 'bg-primary/12 text-primary' },
    more: { icon: 'ri:more-2-fill', class: '' }
  }

  // 获取图标内容
  const iconContent = computed(() => {
    if (props.icon) return props.icon
    if (props.type && props.type in defaultButtons) {
      return defaultButtons[props.type as keyof typeof defaultButtons]?.icon || ''
    }
    return ''
  })

  // 获取按钮样式类
  const buttonClass = computed(() => {
    if (props.iconClass) return props.iconClass
    if (props.type && props.type in defaultButtons) {
      return defaultButtons[props.type as keyof typeof defaultButtons]?.class || ''
    }
    return ''
  })

  const handleClick = () => {
    emit('click')
  }

  const tooltipText = computed(() => props.tooltip || props.label || '')
</script>
