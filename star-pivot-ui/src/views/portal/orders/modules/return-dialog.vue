<!-- C 端申请退货：多 SKU 选择 -->
<template>
  <ElDialog v-model="visible" destroy-on-close title="申请退货" width="640px" @closed="reset">
    <p v-if="!rows.length" class="empty-hint">暂无可退商品</p>
    <template v-else>
      <ElTable
        ref="tableRef"
        :data="rows"
        border
        size="small"
        @selection-change="onSelectionChange"
      >
        <ElTableColumn type="selection" width="48" />
        <ElTableColumn label="商品" min-width="180">
          <template #default="{ row }">
            {{ row.skuName || row.spuName }}
          </template>
        </ElTableColumn>
        <ElTableColumn align="center" label="可退数量" width="88">
          <template #default="{ row }">{{ row.maxQty }}</template>
        </ElTableColumn>
        <ElTableColumn align="center" label="退货数量" width="140">
          <template #default="{ row }">
            <ElInputNumber
              v-model="row.returnQty"
              :disabled="!row.selected"
              :max="row.maxQty"
              :min="1"
              controls-position="right"
              size="small"
            />
          </template>
        </ElTableColumn>
      </ElTable>

      <ElForm ref="formRef" :model="form" :rules="rules" class="return-form" label-width="88px">
        <ElFormItem label="退货原因" prop="reason">
          <ElInput v-model="form.reason" :rows="3" placeholder="如：商品质量问题" type="textarea" />
        </ElFormItem>
        <ElFormItem label="补充说明">
          <ElInput v-model="form.description" :rows="2" placeholder="选填" type="textarea" />
        </ElFormItem>
      </ElForm>
    </template>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton :disabled="!rows.length" :loading="submitting" type="primary" @click="handleSubmit">
        提交申请
      </ElButton>
    </template>
  </ElDialog>
</template>

<script lang="ts" setup>
import type {FormInstance, FormRules, TableInstance} from 'element-plus'
import {ElMessage} from 'element-plus'
import {fetchPortalOrderReturnApply} from '@/api/portal/order'
import type {PortalOrder, PortalOrderItem} from '@/api/portal/types'

interface ReturnRow extends PortalOrderItem {
    maxQty: number
    returnQty: number
    selected: boolean
  }

  const props = defineProps<{
    order: PortalOrder | null
  }>()

  const emit = defineEmits<{
    success: []
  }>()

  const visible = defineModel<boolean>({ default: false })

  const formRef = ref<FormInstance>()
  const tableRef = ref<TableInstance>()
  const submitting = ref(false)
  const rows = ref<ReturnRow[]>([])
  const form = reactive({
    reason: '',
    description: ''
  })

  const rules: FormRules = {
    reason: [{ required: true, message: '请输入退货原因', trigger: 'blur' }]
  }

  function buildRows(order: PortalOrder | null) {
    rows.value = (order?.orderItemList || [])
      .filter((i) => i.skuId != null && (i.skuQuantity || 0) > 0)
      .map((i) => ({
        ...i,
        maxQty: i.skuQuantity || 1,
        returnQty: i.skuQuantity || 1,
        selected: false
      }))
  }

  function onSelectionChange(selection: ReturnRow[]) {
    const selectedIds = new Set(selection.map((r) => r.skuId))
    rows.value.forEach((row) => {
      row.selected = selectedIds.has(row.skuId)
    })
  }

  function reset() {
    rows.value = []
    form.reason = ''
    form.description = ''
    tableRef.value?.clearSelection()
  }

  watch(
    () => [visible.value, props.order?.id] as const,
    ([open, orderId]) => {
      if (!open || !orderId) return
      buildRows(props.order)
      form.reason = ''
      form.description = ''
    }
  )

  async function handleSubmit() {
    if (!props.order?.id) return
    await formRef.value?.validate()
    const selected = rows.value.filter((r) => r.selected)
    if (!selected.length) {
      ElMessage.warning('请至少选择一件退货商品')
      return
    }
    submitting.value = true
    try {
      await fetchPortalOrderReturnApply({
        orderId: props.order.id,
        items: selected.map((r) => ({ skuId: r.skuId!, quantity: r.returnQty })),
        reason: form.reason.trim(),
        description: form.description.trim() || undefined
      })
      visible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>

<style lang="scss" scoped>
  .empty-hint {
    margin: 0;
    color: #999;
    font-size: 14px;
  }

  .return-form {
    margin-top: 16px;
  }
</style>
