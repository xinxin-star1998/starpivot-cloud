<!-- 商城-SKU 查询（只读，仅展示已上架 SPU 下的 SKU） -->
<template>
  <div class="mall-sku-page art-full-height">
    <SkuSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import {h} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useTable} from '@/hooks/core/useTable'
import {fetchMallSkuList, type MallSkuVo} from '@/api/mall/sku'
import {fetchCategoryNameMap, getCategoryDisplayName} from '@/utils/mall/category-tree'
import {fetchBrandNameMap, getBrandDisplayName} from '@/utils/mall/brand-map'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import SkuSearch from './modules/sku-search.vue'
import {ElImage} from 'element-plus'
import {useAuth} from '@/hooks/core/useAuth'
import {getCoverDisplayUrl, resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'

defineOptions({ name: 'MallSku' })

  const skuImgDisplayUrls = ref<Map<string, string>>(new Map())
  const skuImgVersion = ref(0)

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
    spuId: undefined as number | undefined
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
          width: 120,
          fixed: 'right',
          formatter: (row: MallSkuVo) => {
            if (!hasAuth('mall:product:edit') || row.spuId == null) {
              return h('span', { style: 'color: #999' }, '')
            }
            return h(ArtButtonTable, {
              label: '维护 SPU',
              icon: 'ri:links-line',
              iconClass: 'bg-primary/12 text-primary',
              onClick: () => router.push({ path: `/mall/product/edit/${row.spuId}` })
            })
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
    Object.assign(searchParams, {
      pageNum: 1,
      skuName: params.skuName || undefined,
      spuName: params.spuName || undefined,
      spuId
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
