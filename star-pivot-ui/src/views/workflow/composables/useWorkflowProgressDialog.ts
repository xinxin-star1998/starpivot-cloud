/** 流程进度弹窗状态 */
export function useWorkflowProgressDialog() {
  const progressVisible = ref(false)
  const progressInstanceId = ref<number>()

  function openProgressDialog(instanceId?: number) {
    if (!instanceId) return
    progressInstanceId.value = instanceId
    progressVisible.value = true
  }

  return {
    progressVisible,
    progressInstanceId,
    openProgressDialog
  }
}
