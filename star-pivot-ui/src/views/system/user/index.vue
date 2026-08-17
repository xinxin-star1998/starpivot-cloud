<!-- 用户管理页面 -->
<template>
  <div class="user-page art-full-height">
    <div :class="{ 'is-left-collapsed': deptPanelCollapsed }" class="user-layout">
      <DeptTreePanel v-model:collapsed="deptPanelCollapsed" @select="handleDeptSelect" />

      <div class="right-panel">
        <UserSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

        <ElCard class="art-table-card" shadow="never">
          <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
            <template #left>
              <ElSpace wrap>
                <ElButton v-auth="'system:user:add'" v-ripple @click="showDialog('add')">
                  {{ t('system.user.addUser') }}
                </ElButton>
                <ElButton
                  type="danger"
                  :disabled="selectedRows.length === 0"
                  @click="handleBatchDelete"
                  v-ripple
                  v-auth="'system:user:delete'"
                >
                  {{ t('system.user.batchDelete') }}
                </ElButton>
                <ElButton
                  type="primary"
                  plain
                  v-ripple
                  v-auth="'system:user:import'"
                  @click="importDialogVisible = true"
                >
                  {{ t('system.user.importUser') }}
                </ElButton>
                <ElButton
                  type="primary"
                  plain
                  v-ripple
                  v-auth="'system:user:export'"
                  @click="handleExportUsers"
                >
                  {{ t('system.user.exportUser') }}
                </ElButton>
              </ElSpace>
            </template>
          </ArtTableHeader>

          <ExcelImportDialog
            v-model="importDialogVisible"
            :title="t('system.user.importTitle')"
            :show-overwrite="true"
            :overwrite-label="t('system.user.importOverwriteLabel')"
            :import-fn="doImportUser"
            :download-template-fn="fetchDownloadUserImportTemplate"
            @success="refreshData"
          />

          <ArtTable
            :loading="loading"
            :data="data"
            :columns="columns"
            :pagination="pagination"
            @selection-change="handleSelectionChange"
            @pagination:size-change="handleSizeChange"
            @pagination:current-change="handleCurrentChange"
          />

          <UserDialog
            v-model:visible="dialogVisible"
            :type="dialogType"
            :user-data="currentUserData"
            @submit="handleDialogSubmit"
          />
        </ElCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import ExcelImportDialog from '@/components/core/forms/excel-import-dialog/index.vue'
  import { fetchDownloadUserImportTemplate } from '@/api/user/user'
  import { useI18n } from 'vue-i18n'
  import UserSearch from './modules/user-search.vue'
  import UserDialog from './modules/user-dialog.vue'
  import DeptTreePanel from './modules/dept-tree-panel.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useUserPage } from './composables/useUserPage'

  defineOptions({ name: 'User' })

  const { t } = useI18n()

  const {
    searchForm,
    deptPanelCollapsed,
    dialogType,
    dialogVisible,
    currentUserData,
    importDialogVisible,
    selectedRows,
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    showDialog,
    handleSearch,
    handleDeptSelect,
    handleDialogSubmit,
    handleSelectionChange,
    handleBatchDelete,
    handleExportUsers,
    doImportUser
  } = useUserPage()
</script>

<style lang="scss" scoped>
  .user-page {
    --panel-radius: 12px;
    --panel-border: 1px solid var(--art-card-border);
    --panel-shadow: 0 4px 14px rgb(15 23 42 / 6%);
    --panel-shadow-hover: 0 8px 20px rgb(15 23 42 / 10%);
    padding: 0;
    background-color: var(--default-bg-color);
  }

  .user-layout {
    position: relative;
    display: flex;
    gap: 0;
    height: 100%;
    overflow: hidden;
  }

  .right-panel {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;
    background-color: var(--default-box-color);
    border: var(--panel-border);
    border-radius: var(--panel-radius);
    box-shadow: var(--panel-shadow);
    transition: box-shadow 0.25s ease;

    &:hover {
      box-shadow: var(--panel-shadow-hover);
    }
  }

  .dark .right-panel {
    box-shadow: none;
  }

  .art-table-card {
    display: flex;
    flex: 1;
    flex-direction: column;
    border: none;
    box-shadow: none;
  }

  :deep(.el-table) {
    border-radius: 8px;
    border: 1px solid var(--art-card-border);

    .el-table__header-wrapper {
      th {
        font-weight: 600;
        color: var(--art-gray-800);
        background-color: var(--art-gray-100) !important;
      }
    }

    .el-table__body-wrapper {
      tr {
        transition: all 0.2s ease;

        &:hover > td {
          background-color: var(--art-gray-50) !important;
        }
      }
    }
  }

  :deep(.el-button) {
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }

  :deep(.panel-toggle-tooltip) {
    padding: 8px 12px;
    border-radius: 8px;
    font-size: 13px;
    line-height: 1;
  }

  :deep(.el-tag) {
    font-weight: 500;
    border-radius: 6px;
  }

  :deep(.status-indicator) {
    animation: pulse 2s infinite;
  }

  @keyframes pulse {
    0%,
    100% {
      opacity: 1;
    }

    50% {
      opacity: 0.5;
    }
  }
</style>
