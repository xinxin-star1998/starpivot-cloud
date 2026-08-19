<template>
  <view class="page">
    <view v-if="!addresses.length && !showForm" class="empty">
      <text>暂无收货地址</text>
      <view class="empty-actions">
        <button class="btn-outline" :loading="importing" @click="importFromWechat">从微信导入</button>
        <button class="btn" @click="openForm()">新增地址</button>
      </view>
    </view>

    <view v-if="addresses.length && !showForm" class="list">
      <view v-for="item in addresses" :key="item.id" class="card">
        <view class="head">
          <text class="name">{{ item.name }}</text>
          <text class="phone">{{ item.phone }}</text>
          <text v-if="item.defaultStatus === 1" class="tag">默认</text>
        </view>
        <text class="detail">
          {{ item.province }}{{ item.city }}{{ item.region }}{{ item.detailAddress }}
        </text>
        <view class="actions">
          <button size="mini" @click="openForm(item)">编辑</button>
          <button size="mini" @click="setDefault(item)" v-if="item.defaultStatus !== 1">设为默认</button>
          <button size="mini" @click="remove(item.id)">删除</button>
        </view>
      </view>
      <view class="list-actions">
        <button class="btn-outline" :loading="importing" @click="importFromWechat">从微信导入</button>
        <button class="btn-add" @click="openForm()">+ 新增地址</button>
      </view>
    </view>

    <view v-if="showForm" class="form card">
      <view class="field">
        <text class="label">收货人</text>
        <input v-model="form.name" placeholder="姓名" />
      </view>
      <view class="field">
        <text class="label">手机号</text>
        <input v-model="form.phone" type="number" maxlength="11" placeholder="11位手机号" />
      </view>
      <view class="field">
        <text class="label">所在地区</text>
        <RegionPicker ref="regionPickerRef" v-model="region" />
      </view>
      <view class="field">
        <text class="label">详细地址</text>
        <textarea v-model="form.detailAddress" placeholder="街道、门牌号等" />
      </view>
      <view class="field row">
        <text class="label">默认地址</text>
        <switch :checked="form.defaultStatus === 1" @change="onDefaultChange" />
      </view>
      <view class="form-actions">
        <button @click="showForm = false">取消</button>
        <button class="btn-primary" :loading="saving" @click="save">保存</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {nextTick, reactive, ref} from 'vue'
import {fetchAddressList, fetchAddressRemove, fetchAddressSave, fetchAddressUpdate} from '@/pkg-mall/api/address'
import type {PortalAddress, PortalAddressSavePayload} from '@/api/types'
import RegionPicker, {type RegionNames} from '@/components/region-picker.vue'
import {requireLogin} from '@/utils/auth'

const addresses = ref<PortalAddress[]>([])
const showForm = ref(false)
const saving = ref(false)
const importing = ref(false)

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
const region = ref<RegionNames>({ province: '', city: '', region: '' })
const regionPickerRef = ref<InstanceType<typeof RegionPicker>>()

function syncRegionToForm() {
  form.province = region.value.province
  form.city = region.value.city
  form.region = region.value.region
}

async function openForm(item?: PortalAddress) {
  if (item) {
    Object.assign(form, {
      id: item.id,
      name: item.name || '',
      phone: item.phone || '',
      province: item.province || '',
      city: item.city || '',
      region: item.region || '',
      detailAddress: item.detailAddress || '',
      defaultStatus: item.defaultStatus || 0
    })
    region.value = {
      province: item.province || '',
      city: item.city || '',
      region: item.region || ''
    }
  } else {
    Object.assign(form, emptyForm())
    region.value = { province: '', city: '', region: '' }
  }
  showForm.value = true
  await nextTick()
  if (item?.province?.trim()) {
    await regionPickerRef.value?.restoreFromNames(region.value)
  } else {
    regionPickerRef.value?.resetPickerState()
  }
}

function onDefaultChange(e: { detail: { value: boolean } }) {
  form.defaultStatus = e.detail.value ? 1 : 0
}

async function loadList() {
  if (!requireLogin()) return
  addresses.value = await fetchAddressList()
}

async function importFromWechat() {
  if (!requireLogin()) return
  importing.value = true
  try {
    const chosen = await new Promise<UniApp.ChooseAddressSuccess>((resolve, reject) => {
      uni.chooseAddress({
        success: resolve,
        fail: (err) => reject(new Error(err.errMsg || '取消选择地址'))
      })
    })

    const payload: PortalAddressSavePayload = {
      name: chosen.userName || '',
      phone: chosen.telNumber || '',
      postCode: chosen.postalCode || undefined,
      province: chosen.provinceName || '',
      city: chosen.cityName || '',
      region: chosen.countyName || '',
      detailAddress: chosen.detailInfo || '',
      defaultStatus: addresses.value.length ? 0 : 1
    }
    if (!payload.name || !payload.phone || !payload.province || !payload.detailAddress) {
      uni.showToast({ title: '微信地址信息不完整', icon: 'none' })
      return
    }
    await fetchAddressSave(payload)
    uni.showToast({ title: '导入成功' })
    showForm.value = false
    await loadList()
  } catch (e) {
    const msg = (e as Error).message || ''
    if (/cancel|取消|deny|auth|fail cancel/i.test(msg)) {
      return
    }
    if (/requiredPrivateInfos|privacy/i.test(msg)) {
      uni.showToast({ title: '请重新编译小程序后再试微信导入', icon: 'none' })
      return
    }
    uni.showToast({ title: msg || '导入失败', icon: 'none' })
  } finally {
    importing.value = false
  }
}

async function save() {
  syncRegionToForm()
  if (!form.name || !form.phone || !form.province || !form.city || !form.region || !form.detailAddress) {
    uni.showToast({ title: '请填写完整地址', icon: 'none' })
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await fetchAddressUpdate({ ...form })
    } else {
      await fetchAddressSave({ ...form })
    }
    showForm.value = false
    uni.showToast({ title: '保存成功' })
    await loadList()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    saving.value = false
  }
}

async function setDefault(item: PortalAddress) {
  if (!item.id) return
  try {
    await fetchAddressUpdate({ ...item, defaultStatus: 1 } as PortalAddressSavePayload)
    await loadList()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function remove(id?: number) {
  if (!id) return
  try {
    await fetchAddressRemove(id)
    await loadList()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

onShow(loadList)
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 16rpx;
  background: $sp-bg-page;
}
.card {
  margin-bottom: 16rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: $sp-radius-md;
}
.head {
  display: flex;
  gap: 16rpx;
  align-items: center;
}
.name {
  font-size: 30rpx;
  font-weight: 700;
}
.phone {
  font-size: 28rpx;
  color: $sp-text-secondary;
}
.tag {
  padding: 2rpx 10rpx;
  font-size: 20rpx;
  color: #fff;
  background: $sp-primary;
  border-radius: 4rpx;
}
.detail {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: $sp-text-secondary;
  line-height: 1.5;
}
.actions {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;

  button {
    margin: 0;
    color: $sp-text-secondary;
    background: $sp-bg-page;
    border-radius: $sp-radius-pill;
    border: none;

    &::after {
      border: none;
    }
  }
}
.field {
  margin-bottom: 20rpx;
}
.field.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.label {
  display: block;
  margin-bottom: 8rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: $sp-text;
}
input,
textarea {
  width: 100%;
  padding: 16rpx 20rpx;
  background: $sp-bg-page;
  border-radius: $sp-radius-sm;
  font-size: 28rpx;
  box-sizing: border-box;
}
textarea {
  min-height: 120rpx;
}
.form-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;

  button {
    flex: 1;
    margin: 0;
    border-radius: $sp-radius-pill;
    border: none;

    &::after {
      border: none;
    }
  }
}
.btn-primary {
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  color: #fff;
}
.empty {
  padding: 120rpx 0;
  text-align: center;
  color: $sp-text-muted;
}
.empty-actions,
.list-actions {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 24rpx;
}
.btn,
.btn-add,
.btn-outline {
  margin: 0;
  border-radius: $sp-radius-pill;
  border: none;

  &::after {
    border: none;
  }
}
.btn,
.btn-add {
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  color: #fff;
}
.btn-outline {
  color: $sp-primary;
  background: #fff;
  border: 1rpx solid $sp-primary;
}
</style>
