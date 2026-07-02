<template>
  <view class="page">
    <view class="hero">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="title">StarPivot 商城</text>
      <text class="subtitle">欢迎回来，开启品质购物之旅</text>
    </view>

    <view class="card">
      <button class="btn-wechat" :loading="loading" @click="handleWechatLogin">
        <text class="btn-icon">💬</text>
        <text>微信一键登录</text>
      </button>
      <text v-if="USE_MOCK_LOGIN" class="tip">开发模式：使用 Mock code 联调</text>
    </view>

    <view class="divider">
      <view class="divider-line" />
      <text class="divider-text">或使用手机号登录</text>
      <view class="divider-line" />
    </view>

    <view class="card">
      <view class="field">
        <text class="field-label">手机号</text>
        <input v-model="mobile" type="number" maxlength="11" placeholder="请输入手机号" />
      </view>
      <view class="field row">
        <view class="field-grow">
          <text class="field-label">验证码</text>
          <input v-model="smsCode" type="number" maxlength="6" placeholder="请输入验证码" />
        </view>
        <button class="code-btn" size="mini" :disabled="countdown > 0" @click="sendCode">
          {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
        </button>
      </view>
      <button class="btn-sms" :loading="smsLoading" @click="handleSmsLogin">短信登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import {ref} from 'vue'
import {fetchMiniProgramLogin, fetchSmsLogin, fetchSmsSend} from '@/api/auth'
import {MOCK_MINI_LOGIN_CODE, USE_MOCK_LOGIN} from '@/config'
import {setLogin} from '@/stores/member'
import {refreshCartBadge} from '@/utils/tabbar-cart'

const loading = ref(false)
const smsLoading = ref(false)
const mobile = ref('')
const smsCode = ref('')
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

function resolveLoginCode(): Promise<string> {
  if (USE_MOCK_LOGIN) {
    return Promise.resolve(MOCK_MINI_LOGIN_CODE)
  }
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (res) => {
        if (res.code) resolve(res.code)
        else reject(new Error('wx.login 未返回 code'))
      },
      fail: () => reject(new Error('微信登录失败'))
    })
  })
}

async function handleWechatLogin() {
  loading.value = true
  try {
    const code = await resolveLoginCode()
    const result = await fetchMiniProgramLogin(code)
    setLogin(result)
    refreshCartBadge()
    uni.showToast({ title: '登录成功' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function sendCode() {
  if (!/^1[3-9]\d{9}$/.test(mobile.value)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  try {
    await fetchSmsSend(mobile.value)
    uni.showToast({ title: '验证码已发送' })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0 && timer) clearInterval(timer)
    }, 1000)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  }
}

async function handleSmsLogin() {
  smsLoading.value = true
  try {
    const result = await fetchSmsLogin(mobile.value, smsCode.value)
    setLogin(result)
    refreshCartBadge()
    uni.showToast({ title: '登录成功' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    smsLoading.value = false
  }
}
</script>

<style scoped lang="scss">

.page {
  min-height: 100vh;
  padding: 0 32rpx 48rpx;
  background: linear-gradient(180deg, $sp-primary-light 0%, $sp-bg-page 40%);
}

.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0 48rpx;
}

.logo {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 24rpx;
  border-radius: 28rpx;
  box-shadow: $sp-shadow-md;
}

.title {
  font-size: 44rpx;
  font-weight: 800;
  color: $sp-text;
}

.subtitle {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: $sp-text-secondary;
}

.card {
  margin-bottom: 24rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: $sp-radius-lg;
  box-shadow: $sp-shadow-sm;
}

.btn-wechat {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  background: $sp-success;
  color: #fff;
  border-radius: $sp-radius-pill;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 8rpx 20rpx rgba(7, 193, 96, 0.25);

  &::after {
    border: none;
  }
}

.btn-icon {
  font-size: 36rpx;
}

.tip {
  display: block;
  margin-top: 20rpx;
  font-size: 24rpx;
  color: $sp-text-muted;
  text-align: center;
}

.divider {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin: 8rpx 0 32rpx;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background: #e8e8e8;
}

.divider-text {
  font-size: 24rpx;
  color: $sp-text-muted;
  white-space: nowrap;
}

.field {
  margin-bottom: 24rpx;
}

.field-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: $sp-text;
}

.field input {
  padding: 20rpx 24rpx;
  background: $sp-bg-page;
  border-radius: $sp-radius-md;
  font-size: 28rpx;
}

.field.row {
  display: flex;
  gap: 16rpx;
  align-items: flex-end;
}

.field-grow {
  flex: 1;
}

.code-btn {
  margin: 0;
  padding: 0 24rpx;
  height: 72rpx;
  line-height: 72rpx;
  background: $sp-primary-light;
  color: $sp-primary;
  border-radius: $sp-radius-md;
  font-size: 24rpx;
  border: none;
  white-space: nowrap;

  &::after {
    border: none;
  }

  &[disabled] {
    opacity: 0.5;
  }
}

.btn-sms {
  margin-top: 8rpx;
  background: linear-gradient(135deg, $sp-primary 0%, $sp-primary-dark 100%);
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
