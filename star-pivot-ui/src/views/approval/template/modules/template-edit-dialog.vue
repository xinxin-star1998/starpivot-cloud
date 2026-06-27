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
        <ElTableColumn label="策略值" min-width="120">
          <template #default="{ row }">
            <ElInput
              v-model="row.assigneeValue"
              :disabled="!needsAssigneeValue(row.assigneeType)"
              :placeholder="assigneeValuePlaceholder(row.assigneeType)"
              size="small"
            />
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
        <ElTableColumn align="center" fixed="right" label="操作" width="70">
          <template #default="{ $index }">
            <ElButton link type="danger" @click="removeStep($index)">删</ElButton>
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
  import type { ApTemplate, ApTemplateStep } from '@/api/approval/types'
  import { APPROVE_MODE_OPTIONS, ASSIGNEE_TYPE_OPTIONS } from '../../utils/approval-labels'
  import { handleMutationError } from '@/utils/http/mutation'

  const visible = defineModel<boolean>('visible', { default: false })
  const templateId = defineModel<number | undefined>('templateId')

  const emit = defineEmits<{ success: [] }>()

  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  const form = reactive({
    templateId: undefined as number | undefined,
    templateCode: '',
    templateName: '',
    bizModule: 'mall',
    remark: '',
    steps: [] as ApTemplateStep[]
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
      skipExpression: ''
    }
  }

  function needsAssigneeValue(type?: string) {
    return type === 'ROLE' || type === 'POST' || type === 'USER'
  }

  function assigneeValuePlaceholder(type?: string) {
    if (type === 'ROLE') return 'roleKey'
    if (type === 'POST') return 'postCode'
    if (type === 'USER') return '用户ID'
    return '-'
  }

  function addStep() {
    form.steps.push(createEmptyStep(form.steps.length + 1))
  }

  function removeStep(index: number) {
    form.steps.splice(index, 1)
    form.steps.forEach((s, i) => {
      s.stepOrder = i + 1
    })
  }

  async function handleOpen() {
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
    } else {
      form.templateId = undefined
      form.templateCode = ''
      form.templateName = ''
      form.bizModule = 'mall'
      form.remark = ''
      form.steps = [createEmptyStep(1)]
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
        steps
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
</style>
