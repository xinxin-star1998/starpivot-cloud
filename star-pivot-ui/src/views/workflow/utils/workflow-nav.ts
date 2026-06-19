/** 跳转流程实例进度页 */
export function openWorkflowProgress(router: { push: (to: any) => void }, instanceId?: number) {
  if (!instanceId) return
  router.push({
    path: '/workflow/flowchart/index',
    query: { instanceId: String(instanceId) }
  })
}
