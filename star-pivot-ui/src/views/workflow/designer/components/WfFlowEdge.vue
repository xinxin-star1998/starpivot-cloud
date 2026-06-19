<script setup lang="ts">
  import { BaseEdge, getBezierPath, type EdgeProps } from '@vue-flow/core'
  import { computed } from 'vue'

  const props = defineProps<EdgeProps>()

  const path = computed(() => getBezierPath(props))

  const color = computed(() => (props.selected ? '#409eff' : '#64748b'))

  const edgeStyle = computed(() => ({
    stroke: color.value,
    strokeWidth: props.selected ? 2.5 : 2,
    fill: 'none'
  }))
</script>

<script lang="ts">
  export default { inheritAttrs: false }
</script>

<template>
  <BaseEdge
    :id="props.id"
    :path="path[0]"
    :style="edgeStyle"
    :marker-end="props.markerEnd"
    :interaction-width="20"
    :label="props.label"
    :label-x="path[1]"
    :label-y="path[2]"
    :label-show-bg="!!props.label"
    :label-style="props.labelStyle"
    :label-bg-style="props.labelBgStyle"
    :label-bg-padding="props.labelBgPadding"
    :label-bg-border-radius="props.labelBgBorderRadius"
  />
</template>
