<template>
  <ElDialog
    v-model="visible"
    :title="title"
    :width="instanceId ? '820px' : '480px'"
    destroy-on-close
    @closed="emit('closed')"
  >
    <div :class="{ 'has-timeline': !!instanceId }" class="approval-dialog-body">
      <div class="approval-form">
        <div v-if="taskTitle" class="task-summary">{{ taskTitle }}</div>
        <ElDescriptions v-if="taskMeta.length" :column="1" border class="task-meta" size="small">
          <ElDescriptionsItem v-for="item in taskMeta" :key="item.label" :label="item.label">
            {{ item.value }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <ElFormItem class="comment-field" label="审批意见" label-position="top" :required="mode === 'reject'">
          <ElInput
            v-model="comment"
            :rows="5"
            :placeholder="mode === 'reject' ? '驳回时必须填写审批意见' : '审批意见（可选）'"
            type="textarea"
          />
        </ElFormItem>
      </div>

      <div v-if="instanceId" class="approval-timeline-panel">
        <ApprovalTimeline :instance-id="instanceId" />
      </div>
    </div>

    <template #footer>
      <ElButton v-if="bizNav" @click="goBizDetail">{{ bizNav.label }}</ElButton>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton :loading="submitting" type="primary" @click="emit('submit')">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script lang="ts" setup>
import {computed} from 'vue'
import {useRouter} from 'vue-router'
import ApprovalTimeline from '../../components/ApprovalTimeline.vue'
import {resolveApprovalBizNav} from '../../utils/biz-nav'

interface TaskMetaItem {
    label: string
    value?: string
  }

  const props = defineProps<{
    title?: string
    mode?: 'approve' | 'reject'
    submitting?: boolean
    instanceId?: number
    taskTitle?: string
    taskMeta?: TaskMetaItem[]
    bizModule?: string
    bizType?: string
    bizKey?: string
  }>()

  const visible = defineModel<boolean>('visible', { default: false })
  const comment = defineModel<string>('comment', { default: '' })

  const emit = defineEmits<{
    submit: []
    closed: []
  }>()

  const router = useRouter()

  const bizNav = computed(() =>
    resolveApprovalBizNav(props.bizModule, props.bizType, props.bizKey)
  )

  function goBizDetail() {
    const nav = bizNav.value
    if (!nav) return
    visible.value = false
    router.push({ path: nav.path, query: nav.query })
  }
</script>

<style lang="scss" scoped>
  .approval-dialog-body {
    &.has-timeline {
      display: grid;
      grid-template-columns: 300px 1fr;
      gap: 16px;
      align-items: start;
    }
  }

  .task-summary {
    margin-bottom: 12px;
    font-size: 15px;
    font-weight: 600;
  }

  .task-meta {
    margin-bottom: 12px;
  }

  .comment-field {
    margin-bottom: 0;
  }

  .approval-timeline-panel {
    max-height: 420px;
    overflow-y: auto;
    padding: 12px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
  }
</style>
