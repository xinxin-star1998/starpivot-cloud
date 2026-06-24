<!-- 营销-专题活动 -->
<template>
  <div class="sms-subject-page art-full-height">
    <ElCard shadow="never">
      <ElForm :inline="true" @submit.prevent="handleSearch">
        <ElFormItem label="专题名称">
          <ElInput v-model="searchForm.name" clearable placeholder="专题名称" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSelect v-model="searchForm.status" clearable placeholder="全部" style="width: 120px">
            <ElOption label="显示" :value="1" />
            <ElOption label="隐藏" :value="0" />
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
          <ElButton v-auth="'mall:subject:list'" type="primary" @click="openDialog('add')">
            新增专题
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

    <SubjectDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :subject-id="currentId"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { ElImage, ElMessageBox, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchHomeSubjectList,
    fetchHomeSubjectRemove,
    type HomeSubjectVo
  } from '@/api/mall/subject'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import SubjectDialog from './modules/subject-dialog.vue'
  import type { DialogType } from '@/types'
  import { getCoverDisplayUrl, resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'

  defineOptions({ name: 'SmsSubject' })

  const searchForm = ref({ name: undefined as string | undefined, status: undefined as number | undefined })
  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const currentId = ref<number>()

  const imgDisplayUrls = ref<Map<string, string>>(new Map())
  const imgVersion = ref(0)

  const preloadSubjectImages = async (rows: HomeSubjectVo[]) => {
    if (!rows?.length) return
    const imgs = rows.map((row) => row.img?.trim()).filter((url): url is string => !!url)
    if (!imgs.length) return
    const resolved = await resolveGoodsImageDisplayUrls(imgs)
    resolved.forEach((url, key) => imgDisplayUrls.value.set(key, url))
    imgVersion.value++
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
      apiFn: fetchHomeSubjectList,
      apiParams: { pageNum: 1, pageSize: 10, ...searchForm.value },
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 70 },
        { prop: 'name', label: '专题名称', minWidth: 140 },
        {
          prop: 'img',
          label: '图片',
          width: 88,
          formatter: (row: HomeSubjectVo) => {
            void imgVersion.value
            const raw = row.img?.trim()
            if (!raw) return '-'
            const displayUrl = getCoverDisplayUrl(raw, imgDisplayUrls.value)
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
        { prop: 'title', label: '标题', minWidth: 160, showOverflowTooltip: true },
        { prop: 'sort', label: '排序', width: 80 },
        {
          prop: 'status',
          label: '状态',
          width: 90,
          formatter: (row: HomeSubjectVo) =>
            h(ElTag, { type: row.status === 1 ? 'success' : 'info' }, () =>
              row.status === 1 ? '显示' : '隐藏'
            )
        },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: HomeSubjectVo) =>
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
        preloadSubjectImages(rows)
      },
      onCacheHit: (rows) => {
        preloadSubjectImages(rows)
      }
    }
  })

  const handleSearch = () => {
    Object.assign(searchParams, { ...searchForm.value, pageNum: 1 })
    getData()
  }

  const resetSearch = () => {
    searchForm.value = { name: undefined, status: undefined }
    resetSearchParams()
    getData()
  }

  const openDialog = (type: DialogType, id?: number) => {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  const deleteOne = (row: HomeSubjectVo) => {
    if (!row.id) return
    ElMessageBox.confirm(`确定删除专题「${row.name}」吗？`, '删除专题', { type: 'warning' })
      .then(async () => {
        await fetchHomeSubjectRemove([row.id!])
        refreshData()
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .sms-subject-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }
</style>
