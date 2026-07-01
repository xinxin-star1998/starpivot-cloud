<template>
  <ElDialog
    v-model="visible"
    :title="form.templateId ? '编辑审批模板' : '新建审批模板'"
    destroy-on-close
    width="900px"
    @open="handleOpen"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="模板编码" prop="templateCode">
            <ElInput
              v-model="form.templateCode"
              :disabled="!!form.templateId"
              placeholder="如 mall_purchase_default"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="模板名称" prop="templateName">
            <ElInput v-model="form.templateName" placeholder="模板名称" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="业务域" prop="bizModule">
            <ElInput v-model="form.bizModule" placeholder="如 mall" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="备注">
            <ElInput v-model="form.remark" placeholder="备注说明" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <div class="steps-toolbar">
        <span class="steps-title">审批步骤</span>
        <ElButton link type="primary" @click="addStep">添加步骤</ElButton>
      </div>

      <ElTable :data="form.steps" border class="steps-table" size="small">
        <ElTableColumn align="center" label="顺序" width="70">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </ElTableColumn>
        <ElTableColumn label="步骤编码" min-width="120">
          <template #default="{ row }">
            <ElInput v-model="row.stepCode" placeholder="step_code" size="small" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="步骤名称" min-width="120">
          <template #default="{ row }">
            <ElInput v-model="row.stepName" placeholder="步骤名称" size="small" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="审批人策略" min-width="130">
          <template #default="{ row }">
            <ElSelect v-model="row.assigneeType" placeholder="策略" size="small">
              <ElOption
                v-for="opt in ASSIGNEE_TYPE_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="策略值" min-width="160">
          <template #default="{ row }">
            <ElSelect
              v-if="row.assigneeType === 'ROLE'"
              v-model="row.assigneeValue"
              filterable
              placeholder="选择角色"
              size="small"
            >
              <ElOption
                v-for="opt in roleOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </ElSelect>
            <ElSelect
              v-else-if="row.assigneeType === 'POST'"
              v-model="row.assigneeValue"
              filterable
              placeholder="选择岗位"
              size="small"
            >
              <ElOption
                v-for="opt in postOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </ElSelect>
            <ElSelect
              v-else-if="row.assigneeType === 'USER'"
              v-model="row.assigneeValue"
              filterable
              remote
              :remote-method="searchUsers"
              :loading="userLoading"
              placeholder="搜索用户"
              size="small"
            >
              <ElOption
                v-for="opt in userOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </ElSelect>
            <span v-else class="assignee-placeholder">-</span>
          </template>
        </ElTableColumn>
        <ElTableColumn label="通过模式" width="110">
          <template #default="{ row }">
            <ElSelect v-model="row.approveMode" size="small">
              <ElOption
                v-for="opt in APPROVE_MODE_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="跳过表达式" min-width="140">
          <template #default="{ row }">
            <ElInput v-model="row.skipExpression" placeholder="SpEL，可选" size="small" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="超时(小时)" width="110">
          <template #default="{ row }">
            <ElInputNumber
              v-model="row.timeoutHours"
              :min="0"
              controls-position="right"
              placeholder="不启用"
              size="small"
            />
          </template>
        </ElTableColumn>
        <ElTableColumn label="超时策略" width="130">
          <template #default="{ row }">
            <ElSelect
              v-model="row.timeoutAction"
              :disabled="!row.timeoutHours"
              placeholder="策略"
              size="small"
            >
              <ElOption
                v-for="opt in TIMEOUT_ACTION_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn align="center" fixed="right" label="操作" width="70">
          <template #default="{ $index }">
            <ElButton link type="danger" @click="removeStep($index)">删</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>

      <div class="steps-toolbar routes-toolbar">
        <span class="steps-title">条件路由（可选）</span>
        <ElButton link type="primary" @click="addRoute">添加路由</ElButton>
      </div>
      <p class="routes-hint">某步骤完成后按优先级匹配 SpEL，决定跳转目标步骤；留空则按顺序进入下一步。</p>

      <ElTable :data="form.routes" border class="steps-table routes-table" size="small">
        <ElTableColumn label="完成后步骤" min-width="130">
          <template #default="{ row }">
            <ElSelect v-model="row.fromStepCode" placeholder="来源步骤" size="small">
              <ElOption
                v-for="step in form.steps"
                :key="step.stepCode"
                :label="`${step.stepOrder}. ${step.stepName}`"
                :value="step.stepCode"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="条件表达式" min-width="180">
          <template #default="{ row }">
            <ElInput
              v-model="row.conditionExpr"
              placeholder="SpEL 或 default"
              size="small"
            />
          </template>
        </ElTableColumn>
        <ElTableColumn label="跳转步骤" min-width="130">
          <template #default="{ row }">
            <ElSelect v-model="row.toStepCode" placeholder="目标步骤" size="small">
              <ElOption
                v-for="step in form.steps"
                :key="`to-${step.stepCode}`"
                :label="`${step.stepOrder}. ${step.stepName}`"
                :value="step.stepCode"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="优先级" width="90">
          <template #default="{ row }">
            <ElInputNumber v-model="row.priority" :min="0" size="small" controls-position="right" />
          </template>
        </ElTableColumn>
        <ElTableColumn align="center" fixed="right" label="操作" width="70">
          <template #default="{ $index }">
            <ElButton link type="danger" @click="removeRoute($index)">删</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElForm>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton :loading="submitting" type="primary" @click="handleSubmit">保存</ElButton>
    </template>
  </ElDialog>
</template>

<script lang="ts" setup>
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { fetchApprovalTemplateDetail, fetchApprovalTemplateSave } from '@/api/approval/template'
  import type { ApTemplate, ApTemplateRoute, ApTemplateStep } from '@/api/approval/types'
  import { APPROVE_MODE_OPTIONS, ASSIGNEE_TYPE_OPTIONS, TIMEOUT_ACTION_OPTIONS } from '../../utils/approval-labels'
  import { handleMutationError } from '@/utils/http/mutation'
  import { fetchGetRoleSelect } from '@/api/role/role'
  import { fetchGetAllPosts } from '@/api/post/post'
  import { fetchGetUserList } from '@/api/user/user'

  const visible = defineModel<boolean>('visible', { default: false })
  const templateId = defineModel<number | undefined>('templateId')

  const emit = defineEmits<{ success: [] }>()

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const roleOptions = ref<{ label: string; value: string }[]>([])
  const postOptions = ref<{ label: string; value: string }[]>([])
  const userOptions = ref<{ label: string; value: string }[]>([])
  const userLoading = ref(false)

  const form = reactive({
    templateId: undefined as number | undefined,
    templateCode: '',
    templateName: '',
    bizModule: 'mall',
    remark: '',
    steps: [] as ApTemplateStep[],
    routes: [] as ApTemplateRoute[]
  })

  const rules: FormRules = {
    templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
    templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
    bizModule: [{ required: true, message: '请输入业务域', trigger: 'blur' }]
  }

  function createEmptyStep(order: number): ApTemplateStep {
    return {
      stepCode: `step_${order}`,
      stepName: `步骤${order}`,
      stepOrder: order,
      assigneeType: 'DEPT_LEADER',
      assigneeValue: '',
      approveMode: 'ANY',
      skipExpression: '',
      timeoutHours: undefined,
      timeoutAction: 'AUTO_REJECT'
    }
  }

  function needsAssigneeValue(type?: string) {
    return type === 'ROLE' || type === 'POST' || type === 'USER'
  }

  async function loadOrgOptions() {
    try {
      const [roles, posts] = await Promise.all([fetchGetRoleSelect(), fetchGetAllPosts()])
      roleOptions.value = (roles || [])
        .filter((r) => r.roleKey)
        .map((r) => ({ label: `${r.roleName} (${r.roleKey})`, value: r.roleKey! }))
      postOptions.value = (posts || [])
        .filter((p) => p.status !== '1' && p.postCode)
        .map((p) => ({ label: `${p.postName} (${p.postCode})`, value: p.postCode }))
    } catch {
      roleOptions.value = []
      postOptions.value = []
    }
  }

  async function searchUsers(query: string) {
    if (!query?.trim()) {
      userOptions.value = []
      return
    }
    userLoading.value = true
    try {
      const res = await fetchGetUserList({
        pageNum: 1,
        pageSize: 20,
        userName: query.trim()
      })
      userOptions.value = (res.rows || []).map((u) => ({
        label: `${u.nickName || u.userName} (#${u.userId})`,
        value: String(u.userId)
      }))
    } finally {
      userLoading.value = false
    }
  }

  async function preloadUserOption(userId?: string) {
    if (!userId || !/^\d+$/.test(userId)) return
    try {
      const res = await fetchGetUserList({ pageNum: 1, pageSize: 1, userId: Number(userId) })
      const user = res.rows?.[0]
      if (user?.userId) {
        userOptions.value = [
          {
            label: `${user.nickName || user.userName} (#${user.userId})`,
            value: String(user.userId)
          }
        ]
      }
    } catch {
      userOptions.value = [{ label: `用户 #${userId}`, value: userId }]
    }
  }

  function addStep() {
    form.steps.push(createEmptyStep(form.steps.length + 1))
  }

  function removeStep(index: number) {
    form.steps.splice(index, 1)
    form.steps.forEach((s, i) => {
      s.stepOrder = i + 1
    })
    const codes = new Set(form.steps.map((s) => s.stepCode))
    form.routes = form.routes.filter(
      (r) => codes.has(r.fromStepCode || '') && codes.has(r.toStepCode || '')
    )
  }

  function createEmptyRoute(): ApTemplateRoute {
    return {
      fromStepCode: form.steps[0]?.stepCode || '',
      toStepCode: form.steps[1]?.stepCode || form.steps[0]?.stepCode || '',
      priority: form.routes.length,
      conditionExpr: 'default'
    }
  }

  function addRoute() {
    if (!form.steps.length) {
      ElMessage.warning('请先配置审批步骤')
      return
    }
    form.routes.push(createEmptyRoute())
  }

  function removeRoute(index: number) {
    form.routes.splice(index, 1)
  }

  async function handleOpen() {
    await loadOrgOptions()
    if (templateId.value) {
      const detail = await fetchApprovalTemplateDetail(templateId.value)
      const tpl = detail.template as ApTemplate
      form.templateId = tpl.templateId
      form.templateCode = tpl.templateCode || ''
      form.templateName = tpl.templateName || ''
      form.bizModule = tpl.bizModule || 'mall'
      form.remark = tpl.remark || ''
      form.steps = (detail.steps || []).map((s, i) => ({
        ...s,
        stepOrder: i + 1,
        approveMode: s.approveMode || 'ANY'
      }))
      form.routes = (detail.routes || []).map((r) => ({
        fromStepCode: r.fromStepCode,
        toStepCode: r.toStepCode,
        priority: r.priority ?? 0,
        conditionExpr: r.conditionExpr || ''
      }))
      const userStep = form.steps.find((s) => s.assigneeType === 'USER' && s.assigneeValue)
      if (userStep?.assigneeValue) {
        await preloadUserOption(userStep.assigneeValue)
      }
    } else {
      form.templateId = undefined
      form.templateCode = ''
      form.templateName = ''
      form.bizModule = 'mall'
      form.remark = ''
      form.steps = [createEmptyStep(1)]
      form.routes = []
    }
  }

  async function handleSubmit() {
    await formRef.value?.validate()
    if (!form.steps.length) {
      ElMessage.warning('请至少配置一个审批步骤')
      return
    }
    submitting.value = true
    try {
      const steps = form.steps.map((s, i) => ({
        ...s,
        stepOrder: i + 1,
        approveMode: s.approveMode || 'ANY'
      }))
      await fetchApprovalTemplateSave({
        templateId: form.templateId,
        templateCode: form.templateCode,
        templateName: form.templateName,
        bizModule: form.bizModule,
        remark: form.remark,
        steps,
        routes: form.routes
      })
      ElMessage.success('保存成功')
      visible.value = false
      emit('success')
    } catch (error) {
      handleMutationError(error, '保存失败')
    } finally {
      submitting.value = false
    }
  }
</script>

<style lang="scss" scoped>
  .steps-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 8px 0 12px;
  }

  .steps-title {
    font-weight: 600;
  }

  .steps-table {
    width: 100%;
  }

  .routes-toolbar {
    margin-top: 16px;
  }

  .routes-hint {
    margin: 0 0 8px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .assignee-placeholder {
    color: var(--el-text-color-placeholder);
    font-size: 12px;
  }
</style>
