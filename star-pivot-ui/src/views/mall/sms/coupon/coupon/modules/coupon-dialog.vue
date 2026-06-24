<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增优惠券' : '编辑优惠券'"
    width="720px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <ElFormItem label="名称" prop="couponName">
        <ElInput v-model="formData.couponName" maxlength="128" />
      </ElFormItem>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="面额">
            <ElInputNumber
              v-model="formData.amount"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="使用门槛">
            <ElInputNumber
              v-model="formData.minPoint"
              :min="0"
              :precision="2"
              controls-position="right"
              style="width: 100%"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="券类型">
            <ElSelect v-model="formData.couponType" placeholder="类型" style="width: 100%">
              <ElOption
                v-for="opt in COUPON_TYPE_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="适用类型">
            <ElSelect v-model="formData.useType" placeholder="适用范围" style="width: 100%">
              <ElOption
                v-for="opt in COUPON_USE_TYPE_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElFormItem label="有效期">
        <ElDatePicker
          v-model="useDateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="使用开始"
          end-placeholder="使用结束"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="领取时间">
        <ElDatePicker
          v-model="enableDateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="领取开始"
          end-placeholder="领取结束"
          style="width: 100%"
        />
      </ElFormItem>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="发行数量">
            <ElInputNumber v-model="formData.publishCount" :min="0" controls-position="right" style="width: 100%" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="每人限领">
            <ElInputNumber v-model="formData.perLimit" :min="0" controls-position="right" style="width: 100%" />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElFormItem label="发布状态">
        <ElRadioGroup v-model="formData.publish">
          <ElRadio :value="1">已发布</ElRadio>
          <ElRadio :value="0">未发布</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注">
        <ElInput v-model="formData.note" type="textarea" :rows="2" />
      </ElFormItem>

      <ElFormItem v-if="formData.useType === 2" label="关联商品">
        <div class="relation-list">
          <div v-for="(item, index) in formData.spuList" :key="index" class="relation-row">
            <ElInputNumber v-model="item.spuId" :min="1" controls-position="right" placeholder="SPU ID" />
            <ElInput v-model="item.spuName" placeholder="名称（可选）" />
            <ElButton type="danger" link @click="removeSpu(index)">移除</ElButton>
          </div>
          <ElButton type="primary" link @click="addSpu">+ 添加 SPU</ElButton>
        </div>
      </ElFormItem>

      <ElFormItem v-if="formData.useType === 1" label="关联分类">
        <div class="relation-list">
          <div v-for="(item, index) in formData.categoryList" :key="index" class="relation-row">
            <ElInputNumber
              v-model="item.categoryId"
              :min="1"
              controls-position="right"
              placeholder="分类 ID"
            />
            <ElInput v-model="item.categoryName" placeholder="名称（可选）" />
            <ElButton type="danger" link @click="removeCategory(index)">移除</ElButton>
          </div>
          <ElButton type="primary" link @click="addCategory">+ 添加分类</ElButton>
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
    COUPON_TYPE_OPTIONS,
    COUPON_USE_TYPE_OPTIONS,
    fetchCouponAdd,
    fetchCouponById,
    fetchCouponUpdate,
    type CouponSavePayload
  } from '@/api/mall/coupon'
  import type { DialogType } from '@/types'

  interface Props {
    visible: boolean
    type: DialogType
    couponId?: number
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const useDateRange = ref<[string, string] | null>(null)
  const enableDateRange = ref<[string, string] | null>(null)

  const defaultForm = (): CouponSavePayload => ({
    couponName: '',
    useType: 0,
    couponType: 0,
    publish: 0,
    spuList: [],
    categoryList: []
  })

  const formData = ref<CouponSavePayload>(defaultForm())

  const rules: FormRules = {
    couponName: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = defaultForm()
      useDateRange.value = null
      enableDateRange.value = null
      if (props.type === 'edit' && props.couponId) {
        const detail = await fetchCouponById(props.couponId)
        formData.value = {
          id: detail.id,
          couponName: detail.couponName || '',
          couponType: detail.couponType,
          amount: detail.amount,
          minPoint: detail.minPoint,
          perLimit: detail.perLimit,
          useType: detail.useType,
          note: detail.note,
          publishCount: detail.publishCount,
          publish: detail.publish,
          startTime: detail.startTime,
          endTime: detail.endTime,
          enableStartTime: detail.enableStartTime,
          enableEndTime: detail.enableEndTime,
          spuList: (detail.spuList || []).map((s) => ({ spuId: s.spuId, spuName: s.spuName })),
          categoryList: (detail.categoryList || []).map((c) => ({
            categoryId: c.categoryId,
            categoryName: c.categoryName
          }))
        }
        if (detail.startTime && detail.endTime) {
          useDateRange.value = [detail.startTime, detail.endTime]
        }
        if (detail.enableStartTime && detail.enableEndTime) {
          enableDateRange.value = [detail.enableStartTime, detail.enableEndTime]
        }
      }
    }
  )

  const addSpu = () => {
    formData.value.spuList = formData.value.spuList || []
    formData.value.spuList.push({})
  }
  const removeSpu = (index: number) => formData.value.spuList?.splice(index, 1)

  const addCategory = () => {
    formData.value.categoryList = formData.value.categoryList || []
    formData.value.categoryList.push({})
  }
  const removeCategory = (index: number) => formData.value.categoryList?.splice(index, 1)

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      const payload: CouponSavePayload = {
        ...formData.value,
        startTime: useDateRange.value?.[0],
        endTime: useDateRange.value?.[1],
        enableStartTime: enableDateRange.value?.[0],
        enableEndTime: enableDateRange.value?.[1],
        spuList: formData.value.useType === 2
          ? (formData.value.spuList || []).filter((s) => s.spuId)
          : [],
        categoryList: formData.value.useType === 1
          ? (formData.value.categoryList || []).filter((c) => c.categoryId)
          : []
      }
      if (props.type === 'add') await fetchCouponAdd(payload)
      else await fetchCouponUpdate(payload)
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped lang="scss">
  .relation-list {
    width: 100%;
  }
  .relation-row {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
    align-items: center;
  }
</style>
