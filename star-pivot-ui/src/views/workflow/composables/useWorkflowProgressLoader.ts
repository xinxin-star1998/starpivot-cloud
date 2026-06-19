import { fetchWorkflowInstanceProgress, type InstanceProgressVo } from '@/api/workflow/task'

/** 加载流程实例进度 */
export function useWorkflowProgressLoader(getInstanceId: () => number | undefined) {
  const loading = ref(false)
  const progress = ref<InstanceProgressVo | null>(null)

  async function loadProgress() {
    const instanceId = getInstanceId()
    if (!instanceId) {
      progress.value = null
      return
    }
    loading.value = true
    try {
      progress.value = await fetchWorkflowInstanceProgress(instanceId)
    } finally {
      loading.value = false
    }
  }

  function resetProgress() {
    progress.value = null
  }

  return {
    loading,
    progress,
    loadProgress,
    resetProgress
  }
}
