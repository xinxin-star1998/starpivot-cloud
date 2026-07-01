<!-- 分类热门（sms_home_category_hot） -->
<template>
  <div class="cms-category-hot-page art-full-height">
    <ElCard shadow="never">
      <ElForm :inline="true" @submit.prevent="handleSearch">
        <ElFormItem label="展示标题">
          <ElInput v-model="searchForm.title" clearable placeholder="标题" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" clearable placeholder="全部" style="width: 120px">
            <ElOption label="上架" :value="1" />
            <ElOption label="下架" :value="0" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">查询</ElButton>
          <ElButton @click="resetSearch">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton v-auth="'mall:categoryHot:add'" type="primary" @click="openDialog('add')">
            新增分类热门
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <CategoryHotDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :record-id="currentId"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
import {h} from 'vue'
import {ElImage, ElMessageBox, ElTag} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import {
  CATEGORY_HOT_STATUS_MAP,
  fetchCategoryHotList,
  fetchCategoryHotRemove,
  type HomeCategoryHotVo
} from '@/api/mall/category-hot'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import CategoryHotDialog from './modules/category-hot-dialog.vue'
import type {DialogType} from '@/types'
import {getCoverDisplayUrl, resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'

defineOptions({ name: 'CmsCategoryHot' })

  const searchForm = ref({
    title: undefined as string | undefined,
    status: undefined as number | undefined
  })

  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const currentId = ref<number>()

  const iconDisplayUrls = ref<Map<string, string>>(new Map())
  const iconImgVersion = ref(0)

  const preloadIconImages = async (rows: HomeCategoryHotVo[]) => {
    if (!rows?.length) return
    const icons = rows.map((row) => row.icon?.trim()).filter((url): url is string => !!url)
    if (!icons.length) return
    const resolved = await resolveGoodsImageDisplayUrls(icons)
    resolved.forEach((url, key) => iconDisplayUrls.value.set(key, url))
    iconImgVersion.value++
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
      apiFn: fetchCategoryHotList,
      apiParams: { pageNum: 1, pageSize: 10, ...searchForm.value },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 70 },
        { prop: 'catName', label: '关联分类', minWidth: 120, showOverflowTooltip: true },
        { prop: 'title', label: '展示标题', minWidth: 140, showOverflowTooltip: true },
        {
          prop: 'icon',
          label: '图标',
          width: 88,
          formatter: (row: HomeCategoryHotVo) => {
            void iconImgVersion.value
            const raw = row.icon?.trim()
            if (!raw) return '-'
            const displayUrl = getCoverDisplayUrl(raw, iconDisplayUrls.value)
            if (!displayUrl) return '-'
            return h(ElImage, {
              src: displayUrl,
              fit: 'cover',
              previewSrcList: [displayUrl],
              previewTeleported: true,
              style: 'width:56px;height:56px;border-radius:4px'
            })
          }
        },
        { prop: 'sort', label: '排序', width: 70 },
        {
          prop: 'status',
          label: '状态',
          width: 90,
          formatter: (row) =>
            h(
              ElTag,
              { type: row.status === 1 ? 'success' : 'info', size: 'small' },
              () => CATEGORY_HOT_STATUS_MAP[row.status ?? 0] ?? row.status
            )
        },
        { prop: 'url', label: '链接', minWidth: 160, showOverflowTooltip: true },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: HomeCategoryHotVo) =>
            h('div', [
              h(ArtButtonTable, {
                label: '编辑',
                type: 'edit',
                onClick: () => openDialog('edit', row.id)
              }),
              h(ArtButtonTable, {
                label: '删除',
                type: 'delete',
                onClick: () => deleteOne(row)
              })
            ])
        }
      ]
    },
    hooks: {
      onSuccess: (rows) => {
        preloadIconImages(rows)
      },
      onCacheHit: (rows) => {
        preloadIconImages(rows)
      }
    }
  })

  function handleSearch() {
    Object.assign(searchParams, { ...searchForm.value, pageNum: 1 })
    getData()
  }

  function resetSearch() {
    searchForm.value = { title: undefined, status: undefined }
    resetSearchParams()
    getData()
  }

  function openDialog(type: DialogType, id?: number) {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  async function deleteOne(row: HomeCategoryHotVo) {
    if (row.id == null) return
    const label = row.title || row.catName || String(row.id)
    await ElMessageBox.confirm(`确定删除分类热门「${label}」？`, '删除确认', { type: 'warning' })
    await fetchCategoryHotRemove([row.id])
    refreshData()
  }
</script>
