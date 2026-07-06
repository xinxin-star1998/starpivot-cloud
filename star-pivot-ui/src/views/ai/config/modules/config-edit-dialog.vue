<template>
  <ElDialog
    :model-value="visible"
    :title="form.configId ? '编辑 AI 配置' : '新增 AI 配置'"
    width="760px"
    destroy-on-close
    @close="emit('update:visible', false)"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="110px">
      <ElDivider content-position="left">基本信息</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="配置名称" prop="configName">
            <ElInput v-model="form.configName" placeholder="如 default" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="助手名称" prop="botName">
            <ElInput v-model="form.botName" placeholder="AI 助手" />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElFormItem label="助手头像">
        <ElInput v-model="form.botAvatar" placeholder="完整 URL，留空使用默认头像" />
      </ElFormItem>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="状态">
            <ElRadioGroup v-model="form.status">
              <ElRadio value="0">正常</ElRadio>
              <ElRadio value="1">停用</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="设为默认">
            <ElSwitch v-model="isDefaultSwitch" active-text="是" inactive-text="否" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">对话设置</ElDivider>
      <ElFormItem label="欢迎语">
        <ElInput
          v-model="form.welcomeMessage"
          type="textarea"
          :rows="2"
          placeholder="支持 {botName} 占位符"
        />
      </ElFormItem>
      <ElFormItem label="系统提示词" prop="systemPrompt">
        <ElInput v-model="form.systemPrompt" type="textarea" :rows="6" placeholder="定义 AI 助手的行为与能力" />
      </ElFormItem>
      <ElRow :gutter="16">
        <ElCol :span="8">
          <ElFormItem label="默认模型" prop="defaultModel">
            <ElInput v-model="form.defaultModel" placeholder="deepseek-chat" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="8">
          <ElFormItem label="默认温度">
            <ElInputNumber
              v-model="form.defaultTemperature"
              :min="0"
              :max="2"
              :step="0.1"
              :precision="2"
              class="!w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="8">
          <ElFormItem label="记忆条数">
            <ElInputNumber v-model="form.maxMemoryMessages" :min="1" :max="200" class="!w-full" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">RAG 知识库</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="启用 RAG">
            <ElSwitch v-model="ragEnabledSwitch" active-text="开启" inactive-text="关闭" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="检索条数">
            <ElInputNumber v-model="form.ragTopK" :min="1" :max="20" class="!w-full" />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">可选模型</ElDivider>
      <div class="mb-2 flex justify-end">
        <ElButton type="primary" link @click="addModelRow">添加模型</ElButton>
      </div>
      <div v-if="form.models.length" class="space-y-2">
        <div
          v-for="(model, index) in form.models"
          :key="index"
          class="flex items-center gap-2"
        >
          <ElInput v-model="model.id" placeholder="模型 ID" class="flex-1" />
          <ElInput v-model="model.label" placeholder="显示名称" class="flex-1" />
          <ElButton type="danger" link @click="removeModelRow(index)">删除</ElButton>
        </div>
      </div>
      <div v-else class="py-2 text-xs text-g-500">未配置时将仅使用默认模型</div>

      <ElFormItem label="备注" class="mt-4">
        <ElInput v-model="form.remark" type="textarea" :rows="2" />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="emit('update:visible', false)">取消</ElButton>
      <ElButton type="primary" :loading="saving" @click="handleSubmit">保存</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import type { AiConfigItem, AiConfigSavePayload } from '@/api/ai/config'

const props = defineProps<{
  visible: boolean
  saving: boolean
  configData?: AiConfigItem | null
}>()

const emit = defineEmits<{
  'update:visible': [visible: boolean]
  submit: [payload: AiConfigSavePayload]
}>()

const formRef = ref<FormInstance>()

const createEmptyForm = (): AiConfigSavePayload => ({
  configName: '',
  botName: 'AI 助手',
  botAvatar: '',
  welcomeMessage: '',
  systemPrompt: '',
  defaultModel: 'deepseek-chat',
  defaultTemperature: 0.7,
  maxMemoryMessages: 30,
  models: [],
  ragEnabled: '1',
  ragTopK: 5,
  isDefault: '1',
  status: '0',
  remark: ''
})

const form = ref<AiConfigSavePayload>(createEmptyForm())

const isDefaultSwitch = computed({
  get: () => form.value.isDefault === '0',
  set: (value: boolean) => {
    form.value.isDefault = value ? '0' : '1'
  }
})

const ragEnabledSwitch = computed({
  get: () => form.value.ragEnabled === '0',
  set: (value: boolean) => {
    form.value.ragEnabled = value ? '0' : '1'
  }
})

const rules: FormRules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  botName: [{ required: true, message: '请输入助手名称', trigger: 'blur' }],
  systemPrompt: [{ required: true, message: '请输入系统提示词', trigger: 'blur' }],
  defaultModel: [{ required: true, message: '请输入默认模型', trigger: 'blur' }]
}

watch(
  () => props.visible,
  (visible) => {
    if (!visible) return
    if (props.configData?.configId) {
      form.value = {
        configId: props.configData.configId,
        configName: props.configData.configName || '',
        botName: props.configData.botName || 'AI 助手',
        botAvatar: props.configData.botAvatar || '',
        welcomeMessage: props.configData.welcomeMessage || '',
        systemPrompt: props.configData.systemPrompt || '',
        defaultModel: props.configData.defaultModel || 'deepseek-chat',
        defaultTemperature: props.configData.defaultTemperature ?? 0.7,
        maxMemoryMessages: props.configData.maxMemoryMessages ?? 30,
        models: (props.configData.models || []).map((item) => ({ ...item })),
        ragEnabled: props.configData.ragEnabled || '1',
        ragTopK: props.configData.ragTopK ?? 5,
        isDefault: props.configData.isDefault || '1',
        status: props.configData.status || '0',
        remark: props.configData.remark || ''
      }
    } else {
      form.value = createEmptyForm()
    }
    nextTick(() => formRef.value?.clearValidate())
  }
)

function addModelRow(): void {
  form.value.models = form.value.models || []
  form.value.models.push({ id: '', label: '' })
}

function removeModelRow(index: number): void {
  form.value.models?.splice(index, 1)
}

async function handleSubmit(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  emit('submit', {
    ...form.value,
    models: (form.value.models || []).filter((item) => item.id?.trim())
  })
}
</script>
