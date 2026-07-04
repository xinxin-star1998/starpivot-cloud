<template>
  <div :class="{ collapsed: collapsed }" class="left-panel">
    <ElCard class="department-tree-card" shadow="never">
      <div class="department-tree-header">
        <div class="dept-title-row">
          <div class="dept-title">
            <el-icon class="dept-title-icon">
              <OfficeBuilding />
            </el-icon>
            <span>{{ t('system.user.deptTree') }}</span>
          </div>
          <div class="dept-tools">
            <ElButton
              :aria-label="
                deptTreeExpandAll ? t('system.user.collapseDept') : t('system.user.expandDept')
              "
              class="dept-tool-btn"
              text
              @click="toggleDeptTreeExpand"
            >
              <el-icon :class="{ 'is-collapsed': !deptTreeExpandAll }" class="dept-arrow-icon">
                <ArrowDown />
              </el-icon>
            </ElButton>
            <ElButton
              :aria-label="t('system.user.refreshDept')"
              :loading="deptLoading"
              class="dept-tool-btn"
              text
              @click="handleRefreshDeptTree"
            >
              <el-icon>
                <RefreshRight />
              </el-icon>
            </ElButton>
          </div>
        </div>
        <div class="dept-search-box">
          <ElInput
            v-model="deptSearchText"
            :placeholder="t('system.user.searchDept')"
            clearable
            size="small"
            @input="handleDeptSearch"
          >
            <template #prefix>
              <el-icon class="el-input__icon"><Search /></el-icon>
            </template>
          </ElInput>
        </div>
      </div>
      <div class="department-tree-wrapper">
        <ElTree
          :key="deptTreeRenderKey"
          ref="deptTreeRef"
          :data="deptTree"
          :default-checked-keys="selectedDeptId ? [selectedDeptId] : []"
          :default-expand-all="deptTreeExpandAll"
          :expand-on-click-node="false"
          :filter-node-method="filterDeptNode"
          :props="deptTreeProps"
          node-key="deptId"
          @node-click="(data: SysDept) => handleDeptSelect(data.deptId)"
        >
          <template #default="{ node }">
            <span class="custom-tree-node">
              <span>{{ node.label }}</span>
            </span>
          </template>
        </ElTree>
      </div>
    </ElCard>
    <ElTooltip
      :content="collapsed ? t('system.user.expandPanel') : t('system.user.collapsePanel')"
      :show-arrow="true"
      effect="dark"
      placement="right"
      popper-class="panel-toggle-tooltip"
    >
      <ElButton
        :aria-label="
          collapsed ? t('system.user.expandDeptPanel') : t('system.user.collapseDeptPanel')
        "
        class="panel-toggle-btn"
        text
        @click="emit('update:collapsed', !collapsed)"
      >
        <el-icon class="panel-toggle-icon">
          <component :is="collapsed ? DArrowRight : DArrowLeft" />
        </el-icon>
      </ElButton>
    </ElTooltip>
  </div>
</template>

<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {fetchGetDeptTree, type SysDept} from '@/api/dept/dept'
import {ArrowDown, DArrowLeft, DArrowRight, OfficeBuilding, RefreshRight, Search} from '@element-plus/icons-vue'

defineProps<{
    collapsed: boolean
  }>()

  const emit = defineEmits<{
    'update:collapsed': [value: boolean]
    select: [deptId: number | undefined]
  }>()

  const { t } = useI18n()

  const deptTree = ref<SysDept[]>([])
  const deptLoading = ref(false)
  const selectedDeptId = ref<number | undefined>(undefined)
  const deptTreeRef = ref()
  const deptTreeExpandAll = ref(true)
  const deptTreeRenderKey = ref(0)
  const deptSearchText = ref('')

  const deptTreeProps = {
    label: 'deptName',
    children: 'children',
    value: 'deptId'
  }

  const filterDeptNode = (value: string, data: Record<string, unknown>) => {
    if (!value) return true
    return String(data.deptName ?? '').includes(value)
  }

  const handleDeptSearch = () => {
    deptTreeRef.value?.filter(deptSearchText.value)
  }

  const getDeptTree = async () => {
    try {
      deptLoading.value = true
      deptTree.value = await fetchGetDeptTree()
    } catch (error) {
      console.error('获取部门树失败:', error)
    } finally {
      deptLoading.value = false
    }
  }

  const toggleDeptTreeExpand = () => {
    deptTreeExpandAll.value = !deptTreeExpandAll.value
    deptTreeRenderKey.value += 1
  }

  const handleRefreshDeptTree = async () => {
    await getDeptTree()
  }

  const handleDeptSelect = (deptId: number | undefined) => {
    selectedDeptId.value = deptId
    emit('select', deptId)
  }

  onMounted(() => {
    getDeptTree()
  })
</script>

<style lang="scss" scoped>
  .left-panel {
    position: relative;
    display: flex;
    flex-direction: column;
    width: 280px;
    min-width: 280px;
    overflow: visible;
    background-color: var(--default-box-color);
    border: var(--panel-border, 1px solid var(--art-card-border));
    border-radius: var(--panel-radius, 12px);
    box-shadow: var(--panel-shadow, 0 4px 14px rgb(15 23 42 / 6%));
    transition:
      width 0.22s ease,
      min-width 0.22s ease,
      opacity 0.2s ease,
      border-color 0.2s ease,
      box-shadow 0.2s ease;
  }

  .left-panel:hover {
    box-shadow: var(--panel-shadow-hover, 0 8px 20px rgb(15 23 42 / 10%));
  }

  .left-panel.collapsed {
    width: 0;
    min-width: 0;
    border: none;
  }

  .left-panel.collapsed .department-tree-card {
    opacity: 0;
    pointer-events: none;
  }

  .dark .left-panel {
    box-shadow: none;
  }

  .panel-toggle-btn {
    position: absolute;
    top: 50%;
    left: 100%;
    z-index: 3;
    width: 22px;
    height: 40px;
    padding: 0;
    color: var(--el-color-primary);
    border: var(--panel-border, 1px solid var(--art-card-border));
    border-left: none;
    border-radius: 0 10px 10px 0;
    background: var(--el-color-primary-light-9);
    transform: translateY(-50%);
    transition:
      left 0.22s ease,
      color 0.2s ease,
      background-color 0.2s ease;
  }

  .panel-toggle-btn:hover {
    color: var(--el-color-primary-light-3);
    background: var(--el-color-primary-light-8);
    transform: none;
  }

  .panel-toggle-icon {
    font-size: 14px;
  }

  .department-tree-card {
    display: flex;
    flex-direction: column;
    height: 100%;
    border: none;
    box-shadow: none;
  }

  :deep(.department-tree-card > .el-card__body) {
    display: flex;
    flex: 1;
    flex-direction: column;
    height: 100%;
    padding: 0;
  }

  .department-tree-header {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 10px 10px 8px;
    border-bottom: var(--panel-border, 1px solid var(--art-card-border));
    background: linear-gradient(180deg, rgb(64 158 255 / 6%) 0%, transparent 100%);
  }

  .dept-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding-bottom: 2px;
  }

  .dept-title {
    display: inline-flex;
    gap: 6px;
    align-items: center;
    font-size: 14px;
    font-weight: 600;
    color: var(--art-gray-800);
  }

  .dept-title-icon {
    font-size: 13px;
    color: var(--art-gray-500);
  }

  .dept-tools {
    display: inline-flex;
    gap: 6px;
    align-items: center;
  }

  .dept-tool-btn {
    width: 20px;
    height: 20px;
    padding: 0;
    color: var(--art-gray-500);
    border: none;
    border-radius: 4px;
    background: transparent;

    &:hover {
      color: var(--art-gray-700);
      background: var(--art-gray-100);
      transform: none;
    }
  }

  .dept-arrow-icon {
    font-size: 14px;
    transition: transform 0.2s ease;
  }

  .dept-arrow-icon.is-collapsed {
    transform: rotate(-90deg);
  }

  .dept-search-box {
    width: 100%;
  }

  :deep(.el-input) {
    width: 100%;
  }

  .dept-search-box :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: inset 0 0 0 1px var(--art-card-border);
  }

  .dept-search-box :deep(.el-input__wrapper:hover) {
    box-shadow: inset 0 0 0 1px var(--art-gray-400);
  }

  .department-tree-wrapper {
    flex: 1;
    padding: 6px 8px 8px;
    overflow-y: auto;
    background-color: var(--default-box-color);
  }

  .department-tree-wrapper::-webkit-scrollbar {
    width: 6px;
  }

  .department-tree-wrapper::-webkit-scrollbar-thumb {
    border-radius: 999px;
    background: rgb(148 163 184 / 45%);
  }

  :deep(.el-tree) {
    width: 100%;
    height: 100%;
    overflow: auto;
  }

  :deep(.el-tree-node__content) {
    height: 32px;
    line-height: 32px;
    padding-right: 8px;
    border-radius: 6px;
    transition: all 0.2s ease;

    &:hover {
      background-color: var(--art-gray-100);
    }
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    color: var(--art-primary);
    background-color: rgb(64 158 255 / 12%);
  }

  :deep(.el-tree-node__expand-icon) {
    font-size: 14px;
  }

  .custom-tree-node {
    display: flex;
    align-items: center;
    width: 100%;
  }
</style>
