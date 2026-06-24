<!-- 商城-SKU 管理 -->
<template>
  <div class="mall-sku-page art-full-height">
    <SkuSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton v-if="hasAuth('mall:product:add')" type="primary" @click="openCreateDialog">
            新增 SKU
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

    <SkuDialog
      v-model:visible="createDialogVisible"
      :default-spu-id="searchForm.spuId"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { ElImage, ElMessageBox, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchMallSkuList,
    fetchMallSkuPublishStatus,
    fetchMallSkuUpdatePrice,
    type MallSkuVo
  } from '@/api/mall/sku'
  import { fetchCategoryNameMap, getCategoryDisplayName } from '@/utils/mall/category-tree'
  import { fetchBrandNameMap, getBrandDisplayName } from '@/utils/mall/brand-map'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import SkuSearch from './modules/sku-search.vue'
  import SkuDialog from './modules/sku-dialog.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import { getCoverDisplayUrl, resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'

  defineOptions({ name: 'MallSku' })

  const skuImgDisplayUrls = ref<Map<string, string>>(new Map())
  const skuImgVersion = ref(0)
  const createDialogVisible = ref(false)

  const preloadSkuImages = async (rows: MallSkuVo[]) => {
    if (!rows?.length) return
    const imgs = rows.map((row) => row.skuDefaultImg?.trim()).filter((url): url is string => !!url)
    if (!imgs.length) return
    const resolved = await resolveGoodsImageDisplayUrls(imgs)
    resolved.forEach((url, key) => skuImgDisplayUrls.value.set(key, url))
    skuImgVersion.value++
  }

  const { hasAuth } = useAuth()
  const route = useRoute()
  const router = useRouter()

  const searchForm = ref({
    skuName: undefined as string | undefined,
    spuName: undefined as string | undefined,
    spuId: undefined as number | undefined,
    spuPublishStatus: undefined as number | undefined
  })

  const categoryNameMap = ref<Record<number, string>>({})
  const brandNameMap = ref<Record<number, string>>({})

  onMounted(async () => {
    const [catMap, brandMap] = await Promise.all([fetchCategoryNameMap(), fetchBrandNameMap()])
    categoryNameMap.value = catMap
    brandNameMap.value = brandMap
    const q = route.query.spuId
    if (q != null) {
      const id = Number(q)
      if (Number.isFinite(id)) {
        searchForm.value.spuId = id
        Object.assign(searchParams, { spuId: id, pageNum: 1 })
        getData()
      }
    }
  })

  const formatPrice = (row: MallSkuVo) => {
    const p = row.price
    if (p === null || p === undefined || p === '') return '-'
    const n = Number(p)
    return Number.isFinite(n) ? `¥${n.toFixed(2)}` : String(p)
  }

  const openCreateDialog = () => {
    createDialogVisible.value = true
  }

  const editPrice = (row: MallSkuVo) => {
    if (row.skuId == null) return
    const current = row.price != null && row.price !== '' ? Number(row.price) : 0
    ElMessageBox.prompt(`当前价格：${formatPrice(row)}`, `改价 - ${row.skuName || row.skuId}`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^\d+(\.\d{1,2})?$/,
      inputErrorMessage: '请输入有效价格（最多两位小数）',
      inputValue: Number.isFinite(current) ? String(current) : '0'
    })
      .then(async ({ value }) => {
        await fetchMallSkuUpdatePrice(row.skuId!, Number(value))
        refreshData()
      })
      .catch(() => {})
  }

  const togglePublishStatus = (row: MallSkuVo) => {
    if (row.skuId == null) return
    const onShelf = row.spuPublishStatus === 1
    const nextStatus = (onShelf ? 0 : 1) as 0 | 1
    const action = onShelf ? '下架' : '上架'
    ElMessageBox.confirm(
      `确定${action} SKU「${row.skuName || row.skuId}」所属 SPU 吗？${onShelf ? '下架后该 SPU 下全部 SKU 将不再对外展示。' : ''}`,
      `${action}商品`,
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
      .then(async () => {
        await fetchMallSkuPublishStatus(row.skuId!, nextStatus)
        refreshData()
      })
      .catch(() => {})
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
      apiFn: fetchMallSkuList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        {
          type: 'index',
          label: '序号',
          width: 70,
          index: (index: number) => (pagination.current - 1) * pagination.size + index + 1
        },
        {
          prop: 'skuDefaultImg',
          label: '图片',
          width: 88,
          formatter: (row: MallSkuVo) => {
            void skuImgVersion.value
            const raw = row.skuDefaultImg?.trim()
            if (!raw) return '-'
            const displayUrl = getCoverDisplayUrl(raw, skuImgDisplayUrls.value)
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
        { prop: 'skuName', label: 'SKU 名称', minWidth: 180, showOverflowTooltip: true },
        {
          prop: 'saleAttrText',
          label: '销售属性',
          minWidth: 160,
          showOverflowTooltip: true,
          formatter: (row: MallSkuVo) => row.saleAttrText || '-'
        },
        { prop: 'spuName', label: '所属 SPU', minWidth: 140, showOverflowTooltip: true },
        {
          prop: 'price',
          label: '价格',
          width: 110,
          formatter: (row: MallSkuVo) => formatPrice(row)
        },
        {
          prop: 'spuPublishStatus',
          label: '上架状态',
          width: 100,
          formatter: (row: MallSkuVo) => {
            const onShelf = row.spuPublishStatus === 1
            return h(ElTag, { type: onShelf ? 'success' : 'info', size: 'small' }, () =>
              onShelf ? '已上架' : '已下架'
            )
          }
        },
        {
          prop: 'saleCount',
          label: '销量',
          width: 90,
          formatter: (row: MallSkuVo) => row.saleCount ?? 0
        },
        {
          prop: 'catalogId',
          label: '分类',
          minWidth: 120,
          showOverflowTooltip: true,
          formatter: (row: MallSkuVo) =>
            getCategoryDisplayName(categoryNameMap.value, row.catalogId)
        },
        {
          prop: 'brandId',
          label: '品牌',
          minWidth: 120,
          showOverflowTooltip: true,
          formatter: (row: MallSkuVo) => getBrandDisplayName(brandNameMap.value, row.brandId)
        },
        {
          prop: 'operation',
          label: '操作',
          width: 220,
          fixed: 'right',
          formatter: (row: MallSkuVo) => {
            if (!hasAuth('mall:product:edit')) {
              return h('span', { style: 'color: #999' }, '')
            }
            const buttons = [
              h(ArtButtonTable, {
                label: '改价',
                icon: 'ri:money-cny-circle-line',
                iconClass: 'bg-warning/12 text-warning',
                onClick: () => editPrice(row)
              }),
              h(ArtButtonTable, {
                label: row.spuPublishStatus === 1 ? '下架' : '上架',
                icon: row.spuPublishStatus === 1 ? 'ri:arrow-down-line' : 'ri:arrow-up-line',
                iconClass:
                  row.spuPublishStatus === 1
                    ? 'bg-danger/12 text-danger'
                    : 'bg-success/12 text-success',
                onClick: () => togglePublishStatus(row)
              })
            ]
            if (row.spuId != null) {
              buttons.push(
                h(ArtButtonTable, {
                  label: '维护 SPU',
                  icon: 'ri:links-line',
                  iconClass: 'bg-primary/12 text-primary',
                  onClick: () => router.push({ path: `/mall/product/edit/${row.spuId}` })
                })
              )
            }
            return h('div', { class: 'flex flex-wrap gap-1' }, buttons)
          }
        }
      ]
    },
    hooks: {
      onSuccess: (rows) => {
        preloadSkuImages(rows)
      },
      onCacheHit: (rows) => {
        preloadSkuImages(rows)
      }
    }
  })

  watch(
    [categoryNameMap, brandNameMap],
    () => {
      if (data.value?.length) {
        data.value = [...data.value]
      }
    },
    { deep: true }
  )

  const handleSearch = (params: Record<string, unknown>) => {
    const spuIdRaw = params.spuId
    let spuId: number | undefined
    if (spuIdRaw !== '' && spuIdRaw != null) {
      const n = Number(spuIdRaw)
      spuId = Number.isFinite(n) ? n : undefined
    }
    const statusRaw = params.spuPublishStatus
    let spuPublishStatus: number | undefined
    if (statusRaw !== '' && statusRaw != null) {
      const n = Number(statusRaw)
      spuPublishStatus = Number.isFinite(n) ? n : undefined
    }
    Object.assign(searchParams, {
      pageNum: 1,
      skuName: params.skuName || undefined,
      spuName: params.spuName || undefined,
      spuId,
      spuPublishStatus
    })
    getData()
  }
</script>

<style scoped lang="scss">
  .mall-sku-page {
    display: flex;
    flex-direction: column;
    min-height: 0;
  }
</style>
