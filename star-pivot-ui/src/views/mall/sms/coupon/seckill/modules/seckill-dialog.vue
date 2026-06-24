<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增秒杀活动' : '编辑秒杀活动'"
    width="860px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="活动标题" prop="title">
        <ElInput v-model="formData.title" maxlength="255" />
      </ElFormItem>
      <ElFormItem label="活动时间">
        <ElDatePicker
          v-model="dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始"
          end-placeholder="结束"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="状态">
        <ElRadioGroup v-model="formData.status">
          <ElRadio :value="1">上线</ElRadio>
          <ElRadio :value="0">下线</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="秒杀商品">
        <div class="sku-list">
          <div v-for="(item, index) in formData.skuList" :key="index" class="sku-row">
            <ElSelect
              v-model="item.promotionSessionId"
              placeholder="场次"
              clearable
              style="width: 130px"
            >
              <ElOption
                v-for="s in sessionOptions"
                :key="s.id"
                :label="s.name"
                :value="s.id!"
              />
            </ElSelect>
            <ElInputNumber
              v-model="item.skuId"
              :min="1"
              controls-position="right"
              placeholder="SKU ID"
              style="width: 120px"
            />
            <ElInputNumber
              v-model="item.seckillPrice"
              :min="0"
              :precision="2"
              controls-position="right"
              placeholder="秒杀价"
              style="width: 120px"
            />
            <ElInputNumber
              v-model="item.seckillCount"
              :min="0"
              controls-position="right"
              placeholder="总量"
              style="width: 100px"
            />
            <ElInputNumber
              v-model="item.seckillLimit"
              :min="0"
              controls-position="right"
              placeholder="限购"
              style="width: 100px"
            />
            <ElButton type="danger" link @click="removeSku(index)">移除</ElButton>
          </div>
          <ElButton type="primary" link @click="addSku">+ 添加 SKU</ElButton>
        </div>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import {
    fetchSeckillPromotionAdd,
    fetchSeckillPromotionById,
    fetchSeckillPromotionUpdate,
    type SeckillPromotionSavePayload
  } from '@/api/mall/seckill-promotion'
  import { fetchSeckillSessionAll, type SeckillSessionVo } from '@/api/mall/seckill-session'
  import type { DialogType } from '@/types'

  interface Props {
    visible: boolean
    type: DialogType
    promotionId?: number
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const sessionOptions = ref<SeckillSessionVo[]>([])
  const dateRange = ref<[string, string] | null>(null)

  const defaultForm = (): SeckillPromotionSavePayload => ({
    title: '',
    status: 0,
    skuList: []
  })

  const formData = ref<SeckillPromotionSavePayload>(defaultForm())

  const rules: FormRules = {
    title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = defaultForm()
      dateRange.value = null
      sessionOptions.value = await fetchSeckillSessionAll()
      if (props.type === 'edit' && props.promotionId) {
        const detail = await fetchSeckillPromotionById(props.promotionId)
        formData.value = {
          id: detail.id,
          title: detail.title || '',
          startTime: detail.startTime,
          endTime: detail.endTime,
          status: detail.status ?? 0,
          skuList: (detail.skuList || []).map((s) => ({
            promotionSessionId: s.promotionSessionId,
            skuId: s.skuId,
            seckillPrice: s.seckillPrice,
            seckillCount: s.seckillCount,
            seckillLimit: s.seckillLimit,
            seckillSort: s.seckillSort
          }))
        }
        if (detail.startTime && detail.endTime) {
          dateRange.value = [detail.startTime, detail.endTime]
        }
      }
    }
  )

  const addSku = () => {
    formData.value.skuList = formData.value.skuList || []
    formData.value.skuList.push({})
  }

  const removeSku = (index: number) => {
    formData.value.skuList?.splice(index, 1)
  }

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      const payload: SeckillPromotionSavePayload = {
        ...formData.value,
        startTime: dateRange.value?.[0],
        endTime: dateRange.value?.[1],
        skuList: (formData.value.skuList || []).filter((s) => s.skuId)
      }
      if (props.type === 'add') {
        await fetchSeckillPromotionAdd(payload)
      } else {
        await fetchSeckillPromotionUpdate(payload)
      }
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped lang="scss">
  .sku-list {
    width: 100%;
  }
  .sku-row {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
    align-items: center;
    flex-wrap: wrap;
  }
</style>
