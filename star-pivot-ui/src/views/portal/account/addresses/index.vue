<!-- C 端收货地址管理 -->
<template>
  <div v-loading="loading" class="portal-addresses">
    <PortalPageHeader title="收货地址" subtitle="管理您的收货地址">
      <template #extra>
        <ElButton type="primary" @click="openDialog()">新增地址</ElButton>
      </template>
    </PortalPageHeader>

    <div v-if="addresses.length" class="address-list">
      <div v-for="item in addresses" :key="item.id" class="address-card">
        <div class="address-card__head">
          <strong>{{ item.name }}</strong>
          <span>{{ item.phone }}</span>
          <ElTag v-if="item.defaultStatus === 1" type="danger" size="small">默认</ElTag>
        </div>
        <p class="address-card__detail">
          {{ item.province }}{{ item.city }}{{ item.region }}{{ item.detailAddress }}
        </p>
        <div class="address-card__actions">
          <ElButton
            v-if="item.defaultStatus !== 1"
            link
            type="primary"
            :loading="settingDefaultId === item.id"
            @click="handleSetDefault(item)"
          >
            设为默认
          </ElButton>
          <ElButton link type="primary" @click="openDialog(item)">编辑</ElButton>
          <ElButton link type="danger" @click="handleRemove(item.id!)">删除</ElButton>
        </div>
      </div>
    </div>
    <ElEmpty v-else description="暂无收货地址">
      <ElButton type="primary" @click="openDialog()">添加地址</ElButton>
    </ElEmpty>

    <ElDialog v-model="dialogVisible" :title="form.id ? '编辑地址' : '新增地址'" width="520px" destroy-on-close>
      <ElForm ref="formRef" :model="form" :rules="rules" label-width="88px">
        <ElFormItem label="收货人" prop="name">
          <ElInput v-model="form.name" />
        </ElFormItem>
        <ElFormItem label="手机号" prop="phone">
          <ElInput v-model="form.phone" maxlength="11" />
        </ElFormItem>
        <PortalRegionFields ref="regionFieldsRef" :form="form" />
        <ElFormItem label="详细地址" prop="detailAddress">
          <ElInput v-model="form.detailAddress" type="textarea" :rows="2" />
        </ElFormItem>
        <ElFormItem label="默认地址">
          <ElSwitch v-model="defaultSwitch" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="saving" @click="handleSave">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessageBox} from 'element-plus'
import {fetchPortalAddressList, fetchPortalAddressRemove, fetchPortalAddressSave} from '@/api/portal/address'
import type {PortalAddress, PortalAddressSavePayload} from '@/api/portal/types'
import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'
import PortalRegionFields from '@/views/portal/components/portal-region-fields.vue'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {handleMutationError} from '@/utils/http/mutation'

defineOptions({ name: 'PortalAddresses' })

  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const saving = ref(false)
  const settingDefaultId = ref<number>()
  const addresses = ref<PortalAddress[]>([])
  const dialogVisible = ref(false)
  const formRef = ref<FormInstance>()
  const regionFieldsRef = ref<InstanceType<typeof PortalRegionFields>>()
  const defaultSwitch = ref(false)

  const emptyForm = (): PortalAddressSavePayload => ({
    name: '',
    phone: '',
    province: '',
    city: '',
    region: '',
    detailAddress: '',
    defaultStatus: 0
  })

  const form = reactive<PortalAddressSavePayload>(emptyForm())

  const rules: FormRules = {
    name: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
    province: [{ required: true, message: '请选择省份', trigger: 'change' }],
    city: [{ required: true, message: '请选择城市', trigger: 'change' }],
    region: [{ required: true, message: '请选择区县', trigger: 'change' }],
    detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
  }

  async function loadList() {
    if (!requireLogin()) return
    addresses.value = await fetchPortalAddressList()
  }

  function openDialog(item?: PortalAddress) {
    Object.assign(form, item ? { ...item } : emptyForm())
    defaultSwitch.value = form.defaultStatus === 1
    regionFieldsRef.value?.resetRegionPicker()
    dialogVisible.value = true
  }

  async function handleSave() {
    await formRef.value?.validate()
    saving.value = true
    try {
      form.defaultStatus = defaultSwitch.value ? 1 : 0
      await fetchPortalAddressSave({ ...form })
      dialogVisible.value = false
      await loadList()
    } finally {
      saving.value = false
    }
  }

  async function handleSetDefault(item: PortalAddress) {
    settingDefaultId.value = item.id
    try {
      await fetchPortalAddressSave({ ...item, defaultStatus: 1 })
      await loadList()
    } finally {
      settingDefaultId.value = undefined
    }
  }

  async function handleRemove(id: number) {
    try {
      await ElMessageBox.confirm('确定删除该收货地址吗？', '提示', { type: 'warning' })
      await fetchPortalAddressRemove(id)
      await loadList()
    } catch (error) {
      handleMutationError(error, '删除地址失败')
    }
  }

  onMounted(async () => {
    try {
      await loadList()
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  @import '../../styles/variables.scss';

  .address-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .address-card {
    padding: 16px 20px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    background: var(--portal-bg-elevated);

    &__head {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
    }

    &__detail {
      margin: 0 0 12px;
      font-size: 14px;
      color: var(--portal-text-secondary);
      line-height: 1.5;
    }

    &__actions {
      display: flex;
      gap: 8px;
    }
  }
</style>
