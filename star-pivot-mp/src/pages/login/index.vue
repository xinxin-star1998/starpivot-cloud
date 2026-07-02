<template>
  <view class="page">
    <view class="card">
      <button class="btn-primary" :loading="loading" @click="handleWechatLogin">微信一键登录</button>
      <text v-if="USE_MOCK_LOGIN" class="tip">开发模式：使用 Mock code 联调</text>
    </view>

    <view class="card">
      <view class="field">
        <input v-model="mobile" type="number" maxlength="11" placeholder="手机号" />
      </view>
      <view class="field row">
        <input v-model="smsCode" type="number" maxlength="6" placeholder="验证码" />
        <button size="mini" :disabled="countdown > 0" @click="sendCode">
          {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
        </button>
      </view>
      <button class="btn-outline" :loading="smsLoading" @click="handleSmsLogin">短信登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import {ref} from 'vue'
import {fetchMiniProgramLogin, fetchSmsLogin, fetchSmsSend} from '@/api/auth'
import {MOCK_MINI_LOGIN_CODE, USE_MOCK_LOGIN} from '@/config'
import {setLogin} from '@/stores/member'

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
    uni.showToast({ title: '登录成功' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    smsLoading.value = false
  }
}
</script>

<style scoped>
.page {
  padding: 24rpx;
}
.card {
  margin-bottom: 24rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
}
.btn-primary {
  background: #07c160;
  color: #fff;
  border-radius: 12rpx;
}
.btn-outline {
  margin-top: 16rpx;
  background: #1677ff;
  color: #fff;
  border-radius: 12rpx;
}
.tip {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #999;
  text-align: center;
}
.field {
  margin-bottom: 16rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}
.field.row {
  display: flex;
  gap: 16rpx;
  align-items: center;
}
</style>
