<template>
  <view class="page">
    <view class="card">
      <view class="section-head">基本信息</view>
      <view class="avatar-row" @click="chooseAvatar">
        <text class="label">头像</text>
        <view class="avatar-wrap">
          <image class="avatar" :src="form.header || defaultAvatar" mode="aspectFill" />
          <text class="avatar-tip">点击更换</text>
        </view>
      </view>
      <view class="field">
        <text class="label">昵称</text>
        <input v-model="form.nickname" maxlength="64" placeholder="请输入昵称" />
      </view>
      <view class="field">
        <text class="label">性别</text>
        <radio-group class="gender-group" @change="onGenderChange">
          <label class="radio-item"><radio value="0" color="#e1251b" :checked="form.gender === 0" />未知</label>
          <label class="radio-item"><radio value="1" color="#e1251b" :checked="form.gender === 1" />男</label>
          <label class="radio-item"><radio value="2" color="#e1251b" :checked="form.gender === 2" />女</label>
        </radio-group>
      </view>
      <view class="field">
        <text class="label">个性签名</text>
        <textarea v-model="form.sign" maxlength="200" placeholder="写点什么..." />
      </view>
    </view>
    <view class="footer">
      <button class="save-btn" :loading="saving" @click="save">保存资料</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {reactive, ref} from 'vue'
import {uploadImage} from '@/api/image'
import {fetchMemberInfo, updateMemberProfile} from '@/api/member'
import {isLogin, setMember} from '@/stores/member'

const saving = ref(false)
const uploading = ref(false)
const defaultAvatar = 'https://img.yzcdn.cn/vant/cat.jpeg'
const form = reactive({
  nickname: '',
  header: '',
  gender: 0,
  sign: ''
})

async function load() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  const member = await fetchMemberInfo()
  form.nickname = member.nickname || ''
  form.header = member.header || ''
  form.gender = member.gender ?? 0
  form.sign = member.sign || ''
}

function onGenderChange(e: { detail: { value: string } }) {
  form.gender = Number(e.detail.value)
}

function chooseAvatar() {
  if (uploading.value) return
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const filePath = res.tempFilePaths[0]
      if (!filePath) return
      uploading.value = true
      uni.showLoading({ title: '上传中' })
      try {
        const result = await uploadImage(filePath)
        form.header = result.displayUrl || result.permanentUrl || result.objectName
      } catch (e) {
        uni.showToast({ title: (e as Error).message, icon: 'none' })
      } finally {
        uploading.value = false
        uni.hideLoading()
      }
    }
  })
}

async function save() {
  saving.value = true
  try {
    const member = await updateMemberProfile({
      nickname: form.nickname,
      header: form.header || undefined,
      gender: form.gender,
      sign: form.sign
    })
    setMember(member)
    uni.showToast({ title: '保存成功' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    saving.value = false
  }
}

onShow(load)
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 16rpx 16rpx calc(140rpx + env(safe-area-inset-bottom));
  background: $sp-bg-page;
}

.card {
  padding: 28rpx;
  background: #fff;
  border-radius: $sp-radius-md;
}

.section-head {
  margin-bottom: 24rpx;
  padding-left: 12rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: $sp-text;
  border-left: 6rpx solid $sp-primary;
}

.avatar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28rpx;
  padding-bottom: 28rpx;
  border-bottom: 1rpx solid $sp-border;
}

.avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  border: 3rpx solid $sp-primary-light;
  background: #f8f8f8;
}

.avatar-tip {
  font-size: 22rpx;
  color: $sp-primary;
}

.field {
  margin-bottom: 28rpx;
}

.label {
  display: block;
  margin-bottom: 12rpx;
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
  min-height: 160rpx;
}

.gender-group {
  display: flex;
  gap: 32rpx;
}

.radio-item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  color: $sp-text-secondary;
}

.footer {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);
}

.save-btn {
  background: linear-gradient(135deg, $sp-accent 0%, $sp-primary 100%);
  color: #fff;
  border-radius: $sp-radius-pill;
  font-size: 30rpx;
  font-weight: 600;
  border: none;

  &::after {
    border: none;
  }
}
</style>
