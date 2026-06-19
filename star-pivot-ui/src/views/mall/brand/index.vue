<!-- 商城-品牌管理（pms_brand） -->
<template>
  <div class="mall-brand-page art-full-height">
    <BrandSearch v-model="searchForm" @reset="resetSearchParams" @search="handleSearch" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'mall:brand:add'" v-ripple type="primary" @click="showDialog('add')">
              新增品牌
            </ElButton>
            <ElButton
              v-auth="'mall:brand:delete'"
              v-ripple
              :disabled="selectedRows.length === 0"
              type="danger"
              @click="handleBatchDelete"
            >
              批量删除
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <BrandDialog
      v-model:visible="dialogVisible"
      :brand-data="currentBrand"
      :type="dialogType"
      @submit="handleDialogSubmit"
    />

    <BrandCategoryBindDialog
      v-model:visible="bindDialogVisible"
      :brand-id="bindBrand.brandId"
      :brand-name="bindBrand.name"
      @submit="handleBindSubmit"
    />
  </div>
</template>

<script lang="ts" setup>
  import { h } from 'vue'
  import { useTable } from '@/hooks/core/useTable'
  import { fetchMallBrandList, fetchMallBrandRemove, type MallBrandVo } from '@/api/mall/brand'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import BrandSearch from './modules/brand-search.vue'
  import BrandDialog from './modules/brand-dialog.vue'
  import BrandCategoryBindDialog from './modules/brand-category-bind-dialog.vue'
  import { ElImage, ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import type { DialogType } from '@/types'
  import { useAuth } from '@/hooks/core/useAuth'
  import { getCoverDisplayUrl, resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'

  defineOptions({ name: 'MallBrand' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    name: undefined as string | undefined,
    showStatus: undefined as number | undefined,
    firstLetter: undefined as string | undefined
  })

  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentBrand = ref<Partial<MallBrandVo>>({})
  const bindDialogVisible = ref(false)
  const bindBrand = ref<{ brandId?: number; name?: string }>({})
  const selectedRows = ref<MallBrandVo[]>([])

  const logoDisplayUrls = ref<Map<string, string>>(new Map())
  const logoImgVersion = ref(0)

  const preloadLogoImages = async (rows: MallBrandVo[]) => {
    if (!rows?.length) return
    const logos = rows.map((row) => row.logo?.trim()).filter((url): url is string => !!url)
    if (!logos.length) return
    const resolved = await resolveGoodsImageDisplayUrls(logos)
    resolved.forEach((url, key) => logoDisplayUrls.value.set(key, url))
    logoImgVersion.value++
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchMallBrandList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          type: 'index',
          label: '序号',
          width: 70,
          index: (index: number) => (pagination.current - 1) * pagination.size + index + 1
        },
        { prop: 'name', label: '品牌名称', minWidth: 140 },
        {
          prop: 'logo',
          label: 'Logo',
          width: 88,
          formatter: (row: MallBrandVo) => {
            void logoImgVersion.value
            const raw = row.logo?.trim()
            if (!raw) return '-'
            const displayUrl = getCoverDisplayUrl(raw, logoDisplayUrls.value)
            if (!displayUrl) return '-'
            return h(
              'div',
              { class: 'brand-logo-cell' },
              h(ElImage, {
                src: displayUrl,
                fit: 'contain',
                previewSrcList: [displayUrl],
                previewTeleported: true,
                class: 'brand-logo-cell__img'
              })
            )
          }
        },
        { prop: 'descript', label: '介绍', minWidth: 160, showOverflowTooltip: true },
        { prop: 'sort', label: '排序', width: 80 },
        {
          prop: 'showStatus',
          label: '显示',
          width: 100,
          formatter: (row: MallBrandVo) => {
            const s = row.showStatus
            if (s === undefined || s === null) {
              return '-'
            }
            const show = s === 1
            return h(ElTag, { type: show ? 'success' : 'info' }, () => (show ? '显示' : '不显示'))
          }
        },
        { prop: 'firstLetter', label: '首字母', width: 80 },
        {
          prop: 'operation',
          label: '操作',
          width: 225,
          fixed: 'right',
          formatter: (row: MallBrandVo) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:brand:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '绑定分类',
                  icon: 'ri:links-line',
                  iconClass: 'bg-primary/12 text-primary',
                  onClick: () => openBindCategory(row)
                })
              )
            }
            if (hasAuth('mall:brand:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '编辑',
                  type: 'edit',
                  onClick: () => showDialog('edit', row)
                })
              )
            }
            if (hasAuth('mall:brand:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '删除',
                  type: 'delete',
                  onClick: () => deleteOne(row)
                })
              )
            }
            if (actions.length === 0) {
              return h('span', { style: 'color: #999' }, '')
            }
            return h('div', actions)
          }
        }
      ]
    },
    hooks: {
      onSuccess: (rows) => {
        preloadLogoImages(rows)
      },
      onCacheHit: (rows) => {
        preloadLogoImages(rows)
      }
    }
  })

  const handleSearch = (params: Record<string, unknown>) => {
    Object.assign(searchParams, params)
    getData()
  }

  const showDialog = (type: DialogType, row?: MallBrandVo) => {
    dialogType.value = type
    currentBrand.value = row ? { ...row } : {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const openBindCategory = (row: MallBrandVo) => {
    if (row.brandId == null) return
    bindBrand.value = { brandId: row.brandId, name: row.name }
    bindDialogVisible.value = true
  }

  const handleBindSubmit = () => {
    bindBrand.value = {}
  }

  const handleDialogSubmit = () => {
    currentBrand.value = {}
    refreshData()
  }

  const handleSelectionChange = (selection: MallBrandVo[]) => {
    selectedRows.value = selection
  }

  const deleteOne = (row: MallBrandVo) => {
    if (!row.brandId) return
    ElMessageBox.confirm(`确定删除品牌「${row.name || row.brandId}」吗？`, '删除品牌', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await fetchMallBrandRemove([row.brandId!])
        refreshData()
      })
      .catch(() => {})
  }

  const handleBatchDelete = () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的品牌')
      return
    }
    const names = selectedRows.value.map((r) => r.name || r.brandId).join('、')
    ElMessageBox.confirm(`确定删除以下品牌吗？\n${names}`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        const ids = selectedRows.value.map((r) => r.brandId!).filter(Boolean)
        await fetchMallBrandRemove(ids)
        selectedRows.value = []
        refreshData()
      })
      .catch(() => {})
  }
</script>

<style lang="scss" scoped>
  .mall-brand-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }

  :deep(.art-table-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
  }

  :deep(.brand-logo-cell) {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    overflow: hidden;
    background: var(--el-fill-color-lighter);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 4px;

    .brand-logo-cell__img {
      width: 100%;
      height: 100%;
    }
  }
</style>
