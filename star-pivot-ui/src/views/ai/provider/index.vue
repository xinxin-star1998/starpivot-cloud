<template>
  <div class="ai-provider-page art-full-height">
    <div class="page-head">
      <div>
        <h2 class="page-title">{{ t('ai.provider.title') }}</h2>
        <p class="page-desc">{{ t('ai.provider.pageDesc') }}</p>
      </div>
      <div class="page-actions">
        <ElButton :loading="loading" @click="refreshData">{{ t('ai.common.refresh') }}</ElButton>
        <ElButton v-auth="'ai:provider:edit'" type="primary" @click="openEdit()">
          {{ t('ai.provider.add') }}
        </ElButton>
      </div>
    </div>

    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" :model="searchForm" class="search-form">
        <ElFormItem :label="t('ai.provider.name')">
          <ElInput
            v-model="searchForm.providerName"
            clearable
            class="!w-52"
            :placeholder="t('ai.provider.namePlaceholder')"
            @keyup.enter="handleSearch"
          />
        </ElFormItem>
        <ElFormItem :label="t('ai.provider.code')">
          <ElSelect
            v-model="searchForm.providerCode"
            clearable
            :placeholder="t('ai.common.all')"
            class="!w-40"
          >
            <ElOption
              v-for="item in presets"
              :key="item.providerCode"
              :label="item.providerName"
              :value="item.providerCode"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem :label="t('common.status')">
          <ElSelect
            v-model="searchForm.status"
            clearable
            :placeholder="t('ai.common.all')"
            class="!w-28"
          >
            <ElOption :label="t('common.normal')" value="0" />
            <ElOption :label="t('common.disabled')" value="1" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">{{ t('ai.common.search') }}</ElButton>
          <ElButton @click="resetSearch">{{ t('common.reset') }}</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <section v-if="presets.length" class="preset-strip">
      <div class="preset-strip__label">{{ t('ai.provider.quickAdd') }}</div>
      <div class="preset-strip__list">
        <button
          v-for="item in presets"
          :key="item.providerCode"
          class="preset-chip"
          type="button"
          @click="openFromPreset(item.providerCode)"
        >
          <span
            class="vendor-avatar vendor-avatar--sm"
            :style="{ background: resolveVendorMeta(item.providerCode).color }"
          >
            <ArtSvgIcon :icon="resolveVendorMeta(item.providerCode).icon" />
          </span>
          <span>{{ item.providerName }}</span>
        </button>
      </div>
    </section>

    <div v-loading="loading" class="card-wrap">
      <ElEmpty
        v-if="!loading && !data.length"
        :description="t('ai.provider.emptyHint')"
        class="empty-box"
      >
        <ElButton v-auth="'ai:provider:edit'" type="primary" @click="openEdit()">
          {{ t('ai.provider.addFromPreset') }}
        </ElButton>
      </ElEmpty>

      <div v-else class="card-grid">
        <article
          v-for="row in data"
          :key="row.providerId"
          class="provider-card"
          :class="{ 'is-disabled': row.status !== '0' }"
        >
          <div
            class="provider-card__accent"
            :style="{ background: resolveVendorMeta(row.providerCode).color }"
          />
          <header class="provider-card__head">
            <span
              class="vendor-avatar"
              :style="{ background: resolveVendorMeta(row.providerCode).color }"
            >
              <ArtSvgIcon :icon="resolveVendorMeta(row.providerCode).icon" />
            </span>
            <div class="min-w-0 flex-1">
              <div class="provider-card__title">
                <strong>{{ row.providerName }}</strong>
                <ElTag v-if="row.status !== '0'" size="small" type="info">
                  {{ t('common.disabled') }}
                </ElTag>
              </div>
              <p class="provider-card__url" :title="row.baseUrl">{{ row.baseUrl }}</p>
            </div>
            <ElTag :type="row.apiKeyConfigured ? 'success' : 'danger'" size="small" effect="light">
              {{
                row.apiKeyConfigured
                  ? t('ai.provider.apiKeyConfigured')
                  : t('ai.provider.apiKeyMissing')
              }}
            </ElTag>
          </header>

          <div class="provider-card__caps">
            <span
              v-if="row.chatEnabled === '0'"
              class="cap-tag"
              :class="{ 'is-default': row.isDefaultChat === '0' }"
            >
              {{ t('ai.provider.chat') }}
              <em v-if="row.isDefaultChat === '0'">{{ t('ai.provider.defaultBadge') }}</em>
            </span>
            <span
              v-if="row.embeddingEnabled === '0'"
              class="cap-tag"
              :class="{ 'is-default': row.isDefaultEmbedding === '0' }"
            >
              {{ t('ai.provider.embedding') }}
              <em v-if="row.isDefaultEmbedding === '0'">{{ t('ai.provider.defaultBadge') }}</em>
            </span>
            <span
              v-if="row.rerankEnabled === '0'"
              class="cap-tag"
              :class="{ 'is-default': row.isDefaultRerank === '0' }"
            >
              {{ t('ai.provider.rerank') }}
              <em v-if="row.isDefaultRerank === '0'">{{ t('ai.provider.defaultBadge') }}</em>
            </span>
          </div>

          <dl class="provider-card__meta">
            <div v-if="row.defaultChatModel">
              <dt>{{ t('ai.provider.chat') }}</dt>
              <dd>{{ row.defaultChatModel }}</dd>
            </div>
            <div v-if="row.defaultEmbeddingModel">
              <dt>{{ t('ai.provider.embedding') }}</dt>
              <dd>{{ row.defaultEmbeddingModel }}</dd>
            </div>
            <div v-if="row.defaultRerankModel">
              <dt>{{ t('ai.provider.rerank') }}</dt>
              <dd>{{ row.defaultRerankModel }}</dd>
            </div>
            <div>
              <dt>{{ t('ai.provider.models') }}</dt>
              <dd>{{ row.models?.length || 0 }}</dd>
            </div>
          </dl>

          <footer class="provider-card__foot">
            <ElButton link type="primary" @click="openEdit(row)">{{
              t('ai.common.edit')
            }}</ElButton>
            <ElDropdown
              v-if="
                row.chatEnabled === '0' || row.embeddingEnabled === '0' || row.rerankEnabled === '0'
              "
              trigger="click"
              @command="(cmd: string) => handleCardCommand(row, cmd)"
            >
              <ElButton link type="primary">{{ t('ai.provider.more') }}</ElButton>
              <template #dropdown>
                <ElDropdownMenu>
                  <ElDropdownItem
                    v-if="row.chatEnabled === '0' && row.isDefaultChat !== '0'"
                    command="default-chat"
                  >
                    {{ t('ai.provider.setDefaultChat') }}
                  </ElDropdownItem>
                  <ElDropdownItem
                    v-if="row.embeddingEnabled === '0' && row.isDefaultEmbedding !== '0'"
                    command="default-embedding"
                  >
                    {{ t('ai.provider.setDefaultEmbedding') }}
                  </ElDropdownItem>
                  <ElDropdownItem
                    v-if="row.rerankEnabled === '0' && row.isDefaultRerank !== '0'"
                    command="default-rerank"
                  >
                    {{ t('ai.provider.setDefaultRerank') }}
                  </ElDropdownItem>
                  <ElDropdownItem
                    v-if="row.apiKeyConfigured && row.chatEnabled === '0'"
                    command="test-chat"
                  >
                    {{ t('ai.provider.testChat') }}
                  </ElDropdownItem>
                  <ElDropdownItem
                    v-if="row.apiKeyConfigured && row.embeddingEnabled === '0'"
                    command="test-embedding"
                  >
                    {{ t('ai.provider.testEmbedding') }}
                  </ElDropdownItem>
                </ElDropdownMenu>
              </template>
            </ElDropdown>
            <ElButton link type="danger" @click="handleDelete(row)">{{
              t('common.delete')
            }}</ElButton>
          </footer>
        </article>
      </div>
    </div>

    <div v-if="pagination.total > pagination.size" class="pager">
      <ElPagination
        :current-page="pagination.current"
        :page-size="pagination.size"
        :page-sizes="[8, 12, 24]"
        :total="pagination.total"
        background
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <ProviderEditDialog
      v-model:visible="editVisible"
      :provider-data="currentProvider"
      :presets="presets"
      :preset-code="presetCode"
      :saving="saving"
      :testing="testing"
      @submit="handleSave"
      @test="handleTest"
    />
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import {
    type AiProviderItem,
    type AiProviderPreset,
    type AiProviderSavePayload,
    fetchAiProviderDetail,
    fetchAiProviderList,
    fetchAiProviderPresets,
    fetchAiProviderRemove,
    fetchAiProviderSave,
    fetchAiProviderSetDefault,
    fetchAiProviderTest
  } from '@/api/ai/provider'
  import ProviderEditDialog from './modules/provider-edit-dialog.vue'
  import { handleMutationError } from '@/utils/http/mutation'
  import { resolveVendorMeta } from './vendor'

  defineOptions({ name: 'AiProvider' })

  const { t } = useI18n()

  const searchForm = ref({ providerName: '', providerCode: '', status: '' })
  const presets = ref<AiProviderPreset[]>([])
  const editVisible = ref(false)
  const saving = ref(false)
  const testing = ref('')
  const currentProvider = ref<AiProviderItem | null>(null)
  const presetCode = ref('deepseek')

  const {
    data,
    loading,
    pagination,
    searchParams,
    getData,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchAiProviderList,
      apiParams: { pageNum: 1, pageSize: 12, ...searchForm.value }
    }
  })

  async function loadPresets(): Promise<void> {
    try {
      presets.value = (await fetchAiProviderPresets()) || []
    } catch (error) {
      handleMutationError(error, t('ai.common.loadFail'))
    }
  }

  onMounted(() => {
    void loadPresets()
  })

  function handleSearch(): void {
    Object.assign(searchParams, searchForm.value)
    getData()
  }

  function resetSearch(): void {
    searchForm.value = { providerName: '', providerCode: '', status: '' }
    handleSearch()
  }

  async function openEdit(row?: AiProviderItem, code?: string): Promise<void> {
    presetCode.value = code || 'deepseek'
    if (row?.providerId) {
      try {
        currentProvider.value = await fetchAiProviderDetail(row.providerId)
      } catch (error) {
        handleMutationError(error, t('ai.common.loadFail'))
        return
      }
    } else {
      currentProvider.value = null
    }
    editVisible.value = true
  }

  function openFromPreset(code: string): void {
    void openEdit(undefined, code)
  }

  async function handleSave(payload: AiProviderSavePayload): Promise<void> {
    saving.value = true
    try {
      await fetchAiProviderSave(payload)
      ElMessage.success(t('ai.common.saveSuccess'))
      editVisible.value = false
      await refreshData()
    } catch (error) {
      handleMutationError(error, t('ai.common.saveFail'))
    } finally {
      saving.value = false
    }
  }

  async function handleSetDefault(row: AiProviderItem, kind: string): Promise<void> {
    if (!row.providerId) return
    try {
      await fetchAiProviderSetDefault(row.providerId, kind)
      ElMessage.success(t('ai.config.setDefaultSuccess'))
      await refreshData()
    } catch (error) {
      handleMutationError(error, t('ai.config.setDefaultFail'))
    }
  }

  async function handleTest(
    kind: string,
    providerId = currentProvider.value?.providerId
  ): Promise<void> {
    if (!providerId) return
    testing.value = kind
    try {
      const message = await fetchAiProviderTest(providerId, kind)
      ElMessage.success(message || t('ai.provider.testSuccess'))
    } catch (error) {
      handleMutationError(error, t('ai.provider.testFail'))
    } finally {
      testing.value = ''
    }
  }

  function handleCardCommand(row: AiProviderItem, command: string): void {
    if (command.startsWith('default-')) {
      void handleSetDefault(row, command.replace('default-', ''))
      return
    }
    if (command.startsWith('test-')) {
      void handleTest(command.replace('test-', ''), row.providerId)
    }
  }

  async function handleDelete(row: AiProviderItem): Promise<void> {
    if (!row.providerId) return
    try {
      await ElMessageBox.confirm(
        t('ai.provider.deleteConfirm', { name: row.providerName }),
        t('common.tips'),
        {
          type: 'warning',
          confirmButtonText: t('common.delete'),
          cancelButtonText: t('common.cancel')
        }
      )
    } catch {
      return
    }
    try {
      await fetchAiProviderRemove(row.providerId)
      ElMessage.success(t('ai.common.deleteSuccess'))
      await refreshData()
    } catch (error) {
      handleMutationError(error, t('ai.common.deleteFail'))
    }
  }
</script>

<style scoped lang="scss">
  .ai-provider-page {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .page-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
    color: var(--el-text-color-primary);
  }

  .page-desc {
    margin: 4px 0 0;
    max-width: 640px;
    font-size: 13px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);
  }

  .page-actions,
  .search-form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .search-card {
    :deep(.el-card__body) {
      padding-bottom: 2px;
    }
  }

  .preset-strip {
    display: flex;
    align-items: center;
    gap: 12px;
    min-height: 44px;
  }

  .preset-strip__label {
    flex-shrink: 0;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .preset-strip__list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .preset-chip {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px 6px 6px;
    border: 1px solid var(--el-border-color);
    border-radius: 999px;
    background: var(--el-bg-color);
    color: var(--el-text-color-regular);
    font-size: 13px;
    cursor: pointer;
    transition:
      border-color 0.15s ease,
      color 0.15s ease;

    &:hover {
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
    }
  }

  .card-wrap {
    min-height: 240px;
  }

  .empty-box {
    padding: 48px 0;
    background: var(--el-bg-color);
    border: 1px dashed var(--el-border-color);
    border-radius: 12px;
  }

  .card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 12px;
  }

  .provider-card {
    position: relative;
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px 16px 12px;
    overflow: hidden;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 12px;
    background: var(--el-bg-color);
    transition: border-color 0.15s ease;

    &:hover {
      border-color: var(--el-color-primary-light-5);
    }

    &.is-disabled {
      opacity: 0.72;
    }
  }

  .provider-card__accent {
    position: absolute;
    inset: 0 auto 0 0;
    width: 3px;
  }

  .provider-card__head {
    display: flex;
    align-items: flex-start;
    gap: 12px;
  }

  .provider-card__title {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;

    strong {
      overflow: hidden;
      font-size: 15px;
      font-weight: 600;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .provider-card__url {
    margin: 4px 0 0;
    overflow: hidden;
    color: var(--el-text-color-secondary);
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .vendor-avatar {
    display: inline-flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 10px;
    color: #fff;
    font-size: 18px;

    &--sm {
      width: 24px;
      height: 24px;
      border-radius: 999px;
      font-size: 13px;
    }
  }

  .provider-card__caps {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .cap-tag {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 2px 8px;
    border-radius: 999px;
    background: var(--el-fill-color-light);
    color: var(--el-text-color-regular);
    font-size: 12px;

    em {
      font-style: normal;
      color: var(--el-color-success);
    }

    &.is-default {
      background: var(--el-color-success-light-9);
      color: var(--el-color-success);
    }
  }

  .provider-card__meta {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px 12px;
    margin: 0;
    padding: 10px 12px;
    border-radius: 8px;
    background: var(--el-fill-color-lighter);

    div {
      min-width: 0;
    }

    dt {
      color: var(--el-text-color-secondary);
      font-size: 12px;
    }

    dd {
      margin: 2px 0 0;
      overflow: hidden;
      color: var(--el-text-color-primary);
      font-size: 13px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .provider-card__foot {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 4px;
    margin-top: auto;
    padding-top: 4px;
    border-top: 1px solid var(--el-border-color-extra-light);
  }

  .pager {
    display: flex;
    justify-content: flex-end;
  }
</style>
