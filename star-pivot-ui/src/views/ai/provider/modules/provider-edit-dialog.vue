<template>
  <ElDialog
    :model-value="visible"
    :title="form.providerId ? t('ai.provider.edit') : t('ai.provider.add')"
    width="860px"
    align-center
    destroy-on-close
    class="provider-dialog"
    @close="emit('update:visible', false)"
  >
    <div v-if="!form.providerId" class="preset-picker">
      <p class="section-hint">{{ t('ai.provider.presetHint') }}</p>
      <div class="preset-grid">
        <button
          v-for="item in presets"
          :key="item.providerCode"
          class="preset-tile"
          type="button"
          :class="{ 'is-active': form.providerCode === item.providerCode }"
          @click="selectPreset(item.providerCode)"
        >
          <span
            class="vendor-avatar"
            :style="{ background: resolveVendorMeta(item.providerCode).color }"
          >
            <ArtSvgIcon :icon="resolveVendorMeta(item.providerCode).icon" />
          </span>
          <span class="preset-tile__name">{{ item.providerName }}</span>
        </button>
      </div>
    </div>

    <ElForm ref="formRef" :model="form" :rules="rules" label-position="top" class="provider-form">
      <section class="form-section">
        <h3>{{ t('ai.provider.access') }}</h3>
        <ElRow :gutter="16">
          <ElCol :span="12">
            <ElFormItem :label="t('ai.provider.name')" prop="providerName">
              <ElInput v-model="form.providerName" :placeholder="t('ai.provider.namePlaceholder')" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem :label="t('common.status')">
              <ElRadioGroup v-model="form.status">
                <ElRadioButton value="0">{{ t('common.normal') }}</ElRadioButton>
                <ElRadioButton value="1">{{ t('common.disabled') }}</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
        </ElRow>
        <ElFormItem :label="t('ai.provider.baseUrl')" prop="baseUrl">
          <ElInput v-model="form.baseUrl" :placeholder="t('ai.provider.baseUrlPlaceholder')" />
        </ElFormItem>
        <ElFormItem :label="t('ai.provider.apiKey')" prop="apiKey">
          <ElInput
            v-model="form.apiKey"
            type="password"
            show-password
            autocomplete="new-password"
            :placeholder="
              form.providerId ? t('ai.provider.apiKeyPlaceholder') : t('ai.provider.apiKey')
            "
          />
          <p v-if="maskedKey" class="field-extra">{{ t('ai.provider.currentKey') }}：{{ maskedKey }}</p>
        </ElFormItem>
      </section>

      <section class="form-section">
        <h3>{{ t('ai.provider.capabilities') }}</h3>
        <p class="section-hint">{{ t('ai.provider.capabilityHint') }}</p>
        <div class="cap-grid">
          <div class="cap-card" :class="{ 'is-on': chatEnabledSwitch }">
            <div class="cap-card__head">
              <strong>{{ t('ai.provider.chat') }}</strong>
              <ElSwitch v-model="chatEnabledSwitch" />
            </div>
            <ElInput
              v-model="form.defaultChatModel"
              :disabled="!chatEnabledSwitch"
              :placeholder="t('ai.provider.defaultChatModel')"
            />
            <ElCheckbox v-model="defaultChatSwitch" :disabled="!chatEnabledSwitch">
              {{ t('ai.provider.setDefaultChat') }}
            </ElCheckbox>
          </div>
          <div class="cap-card" :class="{ 'is-on': embeddingEnabledSwitch }">
            <div class="cap-card__head">
              <strong>{{ t('ai.provider.embedding') }}</strong>
              <ElSwitch v-model="embeddingEnabledSwitch" />
            </div>
            <ElInput
              v-model="form.defaultEmbeddingModel"
              :disabled="!embeddingEnabledSwitch"
              :placeholder="t('ai.provider.defaultEmbeddingModel')"
            />
            <ElCheckbox v-model="defaultEmbeddingSwitch" :disabled="!embeddingEnabledSwitch">
              {{ t('ai.provider.setDefaultEmbedding') }}
            </ElCheckbox>
          </div>
          <div class="cap-card" :class="{ 'is-on': rerankEnabledSwitch }">
            <div class="cap-card__head">
              <strong>{{ t('ai.provider.rerank') }}</strong>
              <ElSwitch v-model="rerankEnabledSwitch" />
            </div>
            <ElInput
              v-model="form.defaultRerankModel"
              :disabled="!rerankEnabledSwitch"
              :placeholder="t('ai.provider.defaultRerankModel')"
            />
            <ElCheckbox v-model="defaultRerankSwitch" :disabled="!rerankEnabledSwitch">
              {{ t('ai.provider.setDefaultRerank') }}
            </ElCheckbox>
          </div>
        </div>
      </section>

      <section class="form-section">
        <div class="section-head">
          <h3>{{ t('ai.provider.models') }}</h3>
          <div class="section-actions">
            <ElButton type="primary" link @click="syncPresetModels">
              {{ t('ai.provider.syncModels') }}
            </ElButton>
            <ElButton type="primary" link @click="addModelRow">{{ t('ai.provider.addModel') }}</ElButton>
          </div>
        </div>
        <p class="section-hint">{{ t('ai.provider.modelsHint') }}</p>
        <div v-if="form.models.length" class="model-list">
          <div v-for="(model, index) in form.models" :key="index" class="model-row">
            <ElInput v-model="model.id" :placeholder="t('ai.provider.modelId')" />
            <ElInput v-model="model.label" :placeholder="t('ai.provider.modelLabel')" />
            <ElSelect v-model="model.kind" class="!w-28">
              <ElOption :label="t('ai.provider.chat')" value="chat" />
              <ElOption :label="t('ai.provider.embedding')" value="embedding" />
              <ElOption :label="t('ai.provider.rerank')" value="rerank" />
            </ElSelect>
            <ElButton type="danger" link @click="removeModelRow(index)">
              {{ t('common.delete') }}
            </ElButton>
          </div>
        </div>
        <div v-else class="model-empty">{{ t('ai.config.noOptionalModels') }}</div>
      </section>

      <ElCollapse class="advanced-collapse">
        <ElCollapseItem :title="t('ai.provider.advanced')" name="advanced">
          <p class="section-hint">{{ t('ai.provider.pathHint') }}</p>
          <ElRow :gutter="16">
            <ElCol :span="12">
              <ElFormItem :label="t('ai.provider.completionsPath')">
                <ElInput v-model="form.completionsPath" placeholder="/v1/chat/completions" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="12">
              <ElFormItem :label="t('ai.provider.embeddingsPath')">
                <ElInput v-model="form.embeddingsPath" placeholder="/v1/embeddings" />
              </ElFormItem>
            </ElCol>
          </ElRow>
          <ElFormItem :label="t('ai.provider.rerankEndpoint')">
            <ElInput
              v-model="form.rerankEndpoint"
              placeholder="https://dashscope.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank"
            />
          </ElFormItem>
          <ElFormItem :label="t('common.remark')">
            <ElInput v-model="form.remark" type="textarea" :rows="2" />
          </ElFormItem>
        </ElCollapseItem>
      </ElCollapse>
    </ElForm>

    <template #footer>
      <div class="dialog-foot">
        <div class="dialog-foot__left">
          <ElButton
            v-if="form.providerId"
            :loading="testing === 'chat'"
            @click="emit('test', 'chat')"
          >
            {{ t('ai.provider.testChat') }}
          </ElButton>
          <ElButton
            v-if="form.providerId"
            :loading="testing === 'embedding'"
            @click="emit('test', 'embedding')"
          >
            {{ t('ai.provider.testEmbedding') }}
          </ElButton>
        </div>
        <div>
          <ElButton @click="emit('update:visible', false)">{{ t('common.cancel') }}</ElButton>
          <ElButton type="primary" :loading="saving" @click="handleSubmit">
            {{ t('ai.common.save') }}
          </ElButton>
        </div>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import type {
    AiProviderItem,
    AiProviderPreset,
    AiProviderSavePayload
  } from '@/api/ai/provider'
  import { resolveVendorMeta } from '../vendor'

  const props = defineProps<{
    visible: boolean
    saving: boolean
    testing?: string
    providerData?: AiProviderItem | null
    presets: AiProviderPreset[]
    presetCode?: string
  }>()

  const emit = defineEmits<{
    'update:visible': [visible: boolean]
    submit: [payload: AiProviderSavePayload]
    test: [kind: string]
  }>()

  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const maskedKey = ref('')

  const createEmptyForm = (): AiProviderSavePayload => ({
    providerCode: 'deepseek',
    providerName: '',
    baseUrl: 'https://api.deepseek.com',
    apiKey: '',
    completionsPath: '',
    embeddingsPath: '',
    rerankEndpoint: '',
    chatEnabled: '0',
    embeddingEnabled: '1',
    rerankEnabled: '1',
    defaultChatModel: 'deepseek-chat',
    defaultEmbeddingModel: '',
    defaultRerankModel: '',
    models: [],
    isDefaultChat: '1',
    isDefaultEmbedding: '1',
    isDefaultRerank: '1',
    status: '0',
    remark: ''
  })

  const form = ref<AiProviderSavePayload>(createEmptyForm())

  const flagSwitch = (key: 'chatEnabled' | 'embeddingEnabled' | 'rerankEnabled') =>
    computed({
      get: () => form.value[key] === '0',
      set: (value: boolean) => {
        form.value[key] = value ? '0' : '1'
      }
    })

  const defaultSwitch = (key: 'isDefaultChat' | 'isDefaultEmbedding' | 'isDefaultRerank') =>
    computed({
      get: () => form.value[key] === '0',
      set: (value: boolean) => {
        form.value[key] = value ? '0' : '1'
      }
    })

  const chatEnabledSwitch = flagSwitch('chatEnabled')
  const embeddingEnabledSwitch = flagSwitch('embeddingEnabled')
  const rerankEnabledSwitch = flagSwitch('rerankEnabled')
  const defaultChatSwitch = defaultSwitch('isDefaultChat')
  const defaultEmbeddingSwitch = defaultSwitch('isDefaultEmbedding')
  const defaultRerankSwitch = defaultSwitch('isDefaultRerank')

  const rules = computed<FormRules>(() => ({
    providerName: [{ required: true, message: t('ai.provider.nameRequired'), trigger: 'blur' }],
    baseUrl: [{ required: true, message: t('ai.provider.baseUrlRequired'), trigger: 'blur' }],
    apiKey: [
      {
        validator: (_rule, value: string, callback) => {
          if (!form.value.providerId && !value?.trim()) {
            callback(new Error(t('ai.provider.apiKey')))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }))

  function applyPreset(preset?: AiProviderPreset, keepName = false): void {
    if (!preset) return
    const name = keepName && form.value.providerName ? form.value.providerName : preset.providerName
    form.value = {
      ...form.value,
      providerCode: preset.providerCode,
      providerName: name,
      baseUrl: preset.baseUrl || '',
      completionsPath: preset.completionsPath || '',
      embeddingsPath: preset.embeddingsPath || '',
      rerankEndpoint: preset.rerankEndpoint || '',
      chatEnabled: preset.chatEnabled || '0',
      embeddingEnabled: preset.embeddingEnabled || '1',
      rerankEnabled: preset.rerankEnabled || '1',
      defaultChatModel: preset.defaultChatModel || '',
      defaultEmbeddingModel: preset.defaultEmbeddingModel || '',
      defaultRerankModel: preset.defaultRerankModel || '',
      models: (preset.models || []).map((item) => ({ ...item })),
      remark: preset.remark || ''
    }
  }

  function selectPreset(code: string): void {
    if (form.value.providerId) return
    applyPreset(props.presets.find((item) => item.providerCode === code))
  }

  function syncPresetModels(): void {
    const preset = props.presets.find((item) => item.providerCode === form.value.providerCode)
    if (!preset) {
      ElMessage.warning(t('ai.provider.syncModelsEmpty'))
      return
    }
    form.value.models = (preset.models || []).map((item) => ({ ...item }))
    if (preset.defaultChatModel) {
      form.value.defaultChatModel = preset.defaultChatModel
    }
    if (preset.defaultEmbeddingModel) {
      form.value.defaultEmbeddingModel = preset.defaultEmbeddingModel
    }
    if (preset.defaultRerankModel) {
      form.value.defaultRerankModel = preset.defaultRerankModel
    }
    ElMessage.success(t('ai.provider.syncModelsSuccess'))
  }

  watch(
    () => props.visible,
    (visible) => {
      if (!visible) return
      if (props.providerData?.providerId) {
        maskedKey.value = props.providerData.apiKeyMasked || ''
        form.value = {
          providerId: props.providerData.providerId,
          providerCode: props.providerData.providerCode || 'custom',
          providerName: props.providerData.providerName || '',
          baseUrl: props.providerData.baseUrl || '',
          apiKey: '',
          completionsPath: props.providerData.completionsPath || '',
          embeddingsPath: props.providerData.embeddingsPath || '',
          rerankEndpoint: props.providerData.rerankEndpoint || '',
          chatEnabled: props.providerData.chatEnabled || '0',
          embeddingEnabled: props.providerData.embeddingEnabled || '1',
          rerankEnabled: props.providerData.rerankEnabled || '1',
          defaultChatModel: props.providerData.defaultChatModel || '',
          defaultEmbeddingModel: props.providerData.defaultEmbeddingModel || '',
          defaultRerankModel: props.providerData.defaultRerankModel || '',
          models: (props.providerData.models || []).map((item) => ({ ...item })),
          isDefaultChat: props.providerData.isDefaultChat || '1',
          isDefaultEmbedding: props.providerData.isDefaultEmbedding || '1',
          isDefaultRerank: props.providerData.isDefaultRerank || '1',
          status: props.providerData.status || '0',
          remark: props.providerData.remark || ''
        }
      } else {
        maskedKey.value = ''
        form.value = createEmptyForm()
        const code = props.presetCode || 'deepseek'
        applyPreset(props.presets.find((item) => item.providerCode === code) || props.presets[0])
        form.value.isDefaultChat = '0'
      }
      nextTick(() => formRef.value?.clearValidate())
    }
  )

  function addModelRow(): void {
    form.value.models = form.value.models || []
    form.value.models.push({ id: '', label: '', kind: 'chat' })
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

<style scoped lang="scss">
  .preset-picker {
    margin-bottom: 16px;
  }

  .section-hint {
    margin: 0 0 10px;
    color: var(--el-text-color-secondary);
    font-size: 12px;
    line-height: 1.6;
  }

  .preset-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(118px, 1fr));
    gap: 8px;
  }

  .preset-tile {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 12px 8px;
    border: 1px solid var(--el-border-color);
    border-radius: 10px;
    background: var(--el-bg-color);
    cursor: pointer;
    transition: border-color 0.15s ease, background-color 0.15s ease;

    &:hover,
    &.is-active {
      border-color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
    }
  }

  .preset-tile__name {
    overflow: hidden;
    max-width: 100%;
    color: var(--el-text-color-primary);
    font-size: 12px;
    text-align: center;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .vendor-avatar {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 10px;
    color: #fff;
    font-size: 18px;
  }

  .provider-form {
    :deep(.el-form-item) {
      margin-bottom: 14px;
    }
  }

  .form-section {
    margin-bottom: 18px;
    padding: 14px 16px 4px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 10px;
    background: var(--el-fill-color-blank);

    h3 {
      margin: 0 0 12px;
      font-size: 14px;
      font-weight: 600;
    }
  }

  .section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;

    h3 {
      margin: 0;
    }
  }

  .section-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .field-extra {
    margin: 6px 0 0;
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }

  .cap-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
    margin-bottom: 12px;
  }

  .cap-card {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 12px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 10px;
    background: var(--el-fill-color-lighter);

    &.is-on {
      border-color: var(--el-color-primary-light-5);
      background: var(--el-bg-color);
    }
  }

  .cap-card__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .model-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 12px;
  }

  .model-row {
    display: grid;
    grid-template-columns: 1.2fr 1fr 112px auto;
    gap: 8px;
    align-items: center;
  }

  .model-empty {
    margin-bottom: 12px;
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }

  .advanced-collapse {
    border: none;

    :deep(.el-collapse-item__header) {
      font-weight: 600;
    }

    :deep(.el-collapse-item__wrap),
    :deep(.el-collapse-item__header) {
      border: none;
      background: transparent;
    }
  }

  .dialog-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  @media (max-width: 768px) {
    .cap-grid,
    .model-row {
      grid-template-columns: 1fr;
    }
  }
</style>
