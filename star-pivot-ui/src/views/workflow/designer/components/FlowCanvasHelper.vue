<script setup lang="ts">
  import { ref, watch } from 'vue'
  import { useVueFlow, useNodesInitialized } from '@vue-flow/core'

  const props = defineProps<{
    layoutTick: number
  }>()

  const emit = defineEmits<{
    nodesReady: []
  }>()

  const { fitView, getEdges, getNodes, updateNodeInternals } = useVueFlow()
  const nodesInitialized = useNodesInitialized()
  const isDragging = ref(false)

  function refreshNodeInternals() {
    const ids = getNodes.value.map((node) => node.id)
    if (ids.length) updateNodeInternals(ids)
  }

  function fitCanvas(animate = true) {
    if (!nodesInitialized.value || isDragging.value) return
    refreshNodeInternals()
    fitView({ padding: 0.15, duration: animate ? 200 : 0 })
  }

  function onDragStart() {
    isDragging.value = true
  }

  function onDragEnd() {
    isDragging.value = false
  }

  watch(
    nodesInitialized,
    (ready) => {
      if (ready) {
        fitCanvas(false)
        emit('nodesReady')
      }
    },
    { immediate: true }
  )

  watch(
    () => props.layoutTick,
    () => fitCanvas()
  )

  defineExpose({
    fitCanvas,
    onDragStart,
    onDragEnd
  })
</script>

<template><span /></template>
