<!-- 订单设置（菜单路由：/mall/oms/order/level/index） -->
<template>
  <div v-loading="loading" class="order-setting-page art-full-height">
    <ElCard shadow="never">
      <template #header>
        <span class="card-title">订单设置</span>
      </template>
      <ElForm ref="formRef" :model="form" label-width="200px" style="max-width: 640px">
        <ElFormItem label="秒杀订单超时时间(分)">
          <ElInputNumber v-model="form.flashOrderOvertime" :min="1" :max="9999" />
        </ElFormItem>
        <ElFormItem label="正常订单超时时间(分)">
          <ElInputNumber v-model="form.normalOrderOvertime" :min="1" :max="9999" />
        </ElFormItem>
        <ElFormItem label="发货后自动确认收货(天)">
          <ElInputNumber v-model="form.confirmOvertime" :min="1" :max="365" />
        </ElFormItem>
        <ElFormItem label="自动完成交易时间(天)">
          <ElInputNumber v-model="form.finishOvertime" :min="1" :max="365" />
        </ElFormItem>
        <ElFormItem label="订单完成后自动好评(天)">
          <ElInputNumber v-model="form.commentOvertime" :min="1" :max="365" />
        </ElFormItem>
        <ElFormItem label="会员等级（免邮门槛关联）">
          <ElInputNumber v-model="form.memberLevel" :min="0" :max="99" />
        </ElFormItem>
        <ElFormItem>
          <ElButton v-auth="'mall:order:setting'" type="primary" :loading="submitting" @click="handleSave">
            保存设置
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import {
    fetchOrderSetting,
    fetchOrderSettingUpdate,
    type OmsOrderSetting
  } from '@/api/mall/order-setting'

  defineOptions({ name: 'OmsOrderSetting' })

  const loading = ref(false)
  const submitting = ref(false)
  const form = ref<OmsOrderSetting>({
    flashOrderOvertime: 15,
    normalOrderOvertime: 30,
    confirmOvertime: 7,
    finishOvertime: 14,
    commentOvertime: 7,
    memberLevel: 0
  })

  onMounted(loadSetting)

  async function loadSetting() {
    loading.value = true
    try {
      const data = await fetchOrderSetting()
      if (data) {
        form.value = { ...form.value, ...data }
      }
    } finally {
      loading.value = false
    }
  }

  async function handleSave() {
    submitting.value = true
    try {
      await fetchOrderSettingUpdate(form.value)
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
  .card-title {
    font-weight: 600;
  }
</style>
