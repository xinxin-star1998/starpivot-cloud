<template>
  <ElDialog
    v-model="visible"
    destroy-on-close
    title="审批进度"
    width="720px"
    @opened="handleOpened"
  >
    <ApprovalTimeline ref="timelineRef" :instance-id="instanceId" />
  </ElDialog>
</template>

<script lang="ts" setup>
import ApprovalTimeline from './ApprovalTimeline.vue'

const visible = defineModel<boolean>('visible', { default: false })
  const instanceId = defineModel<number | undefined>('instanceId')

  const timelineRef = ref<InstanceType<typeof ApprovalTimeline>>()

  function handleOpened() {
    timelineRef.value?.reload()
  }
</script>
