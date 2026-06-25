<!-- WMS 通用：SKU 远程搜索 + 仓库下拉 -->
<template>
  <div class="wms-stock-selectors">
    <ElFormItem :prop="skuProp" label="商品 SKU">
      <ElSelect
        v-model="skuIdModel"
        :loading="skuLoading"
        :remote-method="searchSku"
        class="w-full"
        clearable
        filterable
        placeholder="输入 SKU 名称搜索"
        remote
        reserve-keyword
        @change="onSkuChange"
      >
        <ElOption
          v-for="item in skuOptions"
          :key="item.skuId"
          :label="formatSkuLabel(item)"
          :value="item.skuId!"
        />
      </ElSelect>
    </ElFormItem>
    <ElFormItem :prop="wareProp" label="仓库">
      <ElSelect
        v-model="wareIdModel"
        :loading="wareLoading"
        class="w-full"
        clearable
        filterable
        placeholder="请选择仓库"
      >
        <ElOption v-for="w in wareOptions" :key="w.id" :label="w.name" :value="w.id!" />
      </ElSelect>
    </ElFormItem>
  </div>
</template>

<script lang="ts" setup>
  import { fetchMallSkuById, fetchMallSkuList, type MallSkuVo } from '@/api/mall/sku'
  import { fetchWmsWareInfoList, type WmsWareInfoVo } from '@/api/mall/wareinfo'

  interface Props {
    skuId?: number
    wareId?: number
    skuProp?: string
    wareProp?: string
  }

  interface Emits {
    (e: 'update:skuId', value?: number): void
    (e: 'update:wareId', value?: number): void
    (e: 'sku-selected', sku?: MallSkuVo): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const skuLoading = ref(false)
  const wareLoading = ref(false)
  const skuOptions = ref<MallSkuVo[]>([])
  const wareOptions = ref<WmsWareInfoVo[]>([])

  const skuIdModel = computed({
    get: () => props.skuId,
    set: (v) => emit('update:skuId', v)
  })

  const wareIdModel = computed({
    get: () => props.wareId,
    set: (v) => emit('update:wareId', v)
  })

  function formatSkuLabel(item: MallSkuVo) {
    const name = item.skuName || `SKU#${item.skuId}`
    return `${name}（ID:${item.skuId}）`
  }

  async function searchSku(keyword: string) {
    skuLoading.value = true
    try {
      const res = await fetchMallSkuList({
        pageNum: 1,
        pageSize: 20,
        skuName: keyword || undefined
      })
      skuOptions.value = res.rows || []
    } catch {
      skuOptions.value = []
    } finally {
      skuLoading.value = false
    }
  }

  async function loadWarehouses() {
    wareLoading.value = true
    try {
      const res = await fetchWmsWareInfoList({ pageNum: 1, pageSize: 200 })
      wareOptions.value = res.rows || []
    } catch {
      wareOptions.value = []
    } finally {
      wareLoading.value = false
    }
  }

  function onSkuChange(skuId?: number) {
    const sku = skuOptions.value.find((s) => s.skuId === skuId)
    emit('sku-selected', sku)
  }

  async function ensureSkuOption(skuId?: number) {
    if (skuId == null) return
    if (skuOptions.value.some((s) => s.skuId === skuId)) return
    try {
      const detail = await fetchMallSkuById(skuId)
      skuOptions.value = [detail, ...skuOptions.value]
    } catch {
      skuOptions.value = [{ skuId, skuName: `SKU#${skuId}` }, ...skuOptions.value]
    }
  }

  onMounted(() => {
    loadWarehouses()
    searchSku('')
    if (props.skuId != null) {
      ensureSkuOption(props.skuId)
    }
  })

  watch(
    () => props.skuId,
    (id) => {
      if (id != null) ensureSkuOption(id)
    }
  )
</script>

<style scoped>
  .w-full {
    width: 100%;
  }
</style>
