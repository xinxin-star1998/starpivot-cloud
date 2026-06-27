export function useApprovalTimelineDialog() {
  const progressVisible = ref(false)
  const progressInstanceId = ref<number>()

  function openTimelineDialog(instanceId?: number) {
    if (!instanceId) return
    progressInstanceId.value = instanceId
    progressVisible.value = true
  }

  return {
    progressVisible,
    progressInstanceId,
    openTimelineDialog
  }
}
