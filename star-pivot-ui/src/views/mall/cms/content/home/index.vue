<!-- 首页轮播（sms_home_adv） -->
<template>
  <div class="cms-home-adv-page art-full-height">
    <ElCard shadow="never">
      <ElForm :inline="true" @submit.prevent="handleSearch">
        <ElFormItem label="广告名称">
          <ElInput v-model="searchForm.name" clearable placeholder="名称" />
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
          <ElButton v-auth="'mall:adv:add'" type="primary" @click="openDialog('add')">
            新增轮播
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

    <HomeAdvDialog
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
import {fetchHomeAdvList, fetchHomeAdvRemove, HOME_ADV_STATUS_MAP, type HomeAdvVo} from '@/api/mall/home-adv'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import HomeAdvDialog from './modules/home-adv-dialog.vue'
import type {DialogType} from '@/types'
import {getCoverDisplayUrl, resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'

defineOptions({ name: 'CmsHomeAdv' })

  const searchForm = ref({
    name: undefined as string | undefined,
    status: undefined as number | undefined
  })

  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const currentId = ref<number>()

  const picDisplayUrls = ref<Map<string, string>>(new Map())
  const picImgVersion = ref(0)

  const preloadPicImages = async (rows: HomeAdvVo[]) => {
    if (!rows?.length) return
    const pics = rows.map((row) => row.pic?.trim()).filter((url): url is string => !!url)
    if (!pics.length) return
    const resolved = await resolveGoodsImageDisplayUrls(pics)
    resolved.forEach((url, key) => picDisplayUrls.value.set(key, url))
    picImgVersion.value++
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
      apiFn: fetchHomeAdvList,
      apiParams: { pageNum: 1, pageSize: 10, ...searchForm.value },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 70 },
        { prop: 'name', label: '名称', minWidth: 140, showOverflowTooltip: true },
        {
          prop: 'pic',
          label: '图片',
          width: 88,
          formatter: (row: HomeAdvVo) => {
            void picImgVersion.value
            const raw = row.pic?.trim()
            if (!raw) return '-'
            const displayUrl = getCoverDisplayUrl(raw, picDisplayUrls.value)
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
              () => HOME_ADV_STATUS_MAP[row.status ?? 0] ?? row.status
            )
        },
        { prop: 'clickCount', label: '点击', width: 80 },
        { prop: 'startTime', label: '开始', minWidth: 160 },
        { prop: 'endTime', label: '结束', minWidth: 160 },
        { prop: 'url', label: '链接', minWidth: 160, showOverflowTooltip: true },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: HomeAdvVo) =>
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
        preloadPicImages(rows)
      },
      onCacheHit: (rows) => {
        preloadPicImages(rows)
      }
    }
  })

  function handleSearch() {
    Object.assign(searchParams, { ...searchForm.value, pageNum: 1 })
    getData()
  }

  function resetSearch() {
    searchForm.value = { name: undefined, status: undefined }
    resetSearchParams()
    getData()
  }

  function openDialog(type: DialogType, id?: number) {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  async function deleteOne(row: HomeAdvVo) {
    if (row.id == null) return
    await ElMessageBox.confirm(`确定删除轮播「${row.name}」？`, '删除确认', { type: 'warning' })
    await fetchHomeAdvRemove([row.id])
    refreshData()
  }
</script>
