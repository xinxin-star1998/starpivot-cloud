<template>
  <view class="page">
    <view v-if="loading" class="hint">加载中...</view>
    <template v-else>
      <view class="card">
        <view class="section-title">已绑定登录方式</view>
        <view v-if="bindings.length" class="binding-list">
          <view
            v-for="item in bindings"
            :key="`${item.authType}-${item.identifier}`"
            class="binding-item"
          >
            <view class="binding-icon">{{ authIcon(item.authType) }}</view>
            <view class="binding-body">
              <text class="binding-label">{{ item.authTypeLabel }}</text>
              <text class="binding-id">{{ item.maskedIdentifier || item.identifier }}</text>
              <text v-if="item.bindTime" class="binding-time">绑定于 {{ item.bindTime }}</text>
            </view>
            <button
              v-if="item.authType === AUTH_TYPE_MOBILE && canUnbindMobile"
              class="unlink-btn"
              size="mini"
              @click="openUnbindDialog"
            >
              解绑
            </button>
            <text v-else-if="item.authType === AUTH_TYPE_PASSWORD" class="tag ok">已启用</text>
            <text v-else class="tag">已绑定</text>
          </view>
        </view>
        <view v-else class="hint-inline">暂无绑定记录</view>
      </view>

      <view class="card actions">
        <view v-if="!hasMobileBinding" class="action-item" @click="openBindDialog">
          <text class="action-title">绑定手机号</text>
          <text class="action-desc">用于验证码登录与安全验证</text>
        </view>
        <view
          class="action-item"
          :class="{ disabled: !hasMobileBinding }"
          @click="openPasswordDialog"
        >
          <text class="action-title">{{ hasPasswordBinding ? '修改登录密码' : '设置登录密码' }}</text>
          <text class="action-desc">
            {{ hasMobileBinding ? '需已绑定手机号接收验证码' : '请先绑定手机号' }}
          </text>
        </view>
      </view>
    </template>

    <!-- 绑定手机号 -->
    <view v-if="bindVisible" class="modal-mask" @click="bindVisible = false">
      <view class="modal" @click.stop>
        <text class="modal-title">绑定手机号</text>
        <input v-model="bindForm.mobile" class="input" maxlength="11" placeholder="手机号" />
        <view class="sms-row">
          <input v-model="bindForm.code" class="input flex" maxlength="6" placeholder="验证码" />
          <button
            class="sms-btn"
            size="mini"
            :disabled="bindCountdown > 0 || bindSending"
            @click="sendBindCode"
          >
            {{ bindCountdown > 0 ? `${bindCountdown}s` : '获取验证码' }}
          </button>
        </view>
        <text v-if="authConfig?.smsMockEnabled" class="mock-tip">开发模式验证码：123456</text>
        <view class="modal-actions">
          <button @click="bindVisible = false">取消</button>
          <button class="primary" :loading="bindSubmitting" @click="submitBind">确认绑定</button>
        </view>
      </view>
    </view>

    <!-- 设置密码 -->
    <view v-if="passwordVisible" class="modal-mask" @click="passwordVisible = false">
      <view class="modal" @click.stop>
        <text class="modal-title">设置登录密码</text>
        <input v-model="passwordForm.password" class="input" password placeholder="6-32 位新密码" />
        <input
          v-model="passwordForm.confirmPassword"
          class="input"
          password
          placeholder="再次输入密码"
        />
        <view class="sms-row">
          <input v-model="passwordForm.code" class="input flex" maxlength="6" placeholder="验证码" />
          <button
            class="sms-btn"
            size="mini"
            :disabled="passwordCountdown > 0 || passwordSending"
            @click="sendPasswordCode"
          >
            {{ passwordCountdown > 0 ? `${passwordCountdown}s` : '获取验证码' }}
          </button>
        </view>
        <text v-if="boundMobile" class="mock-tip">验证码将发送至 {{ maskMobile(boundMobile) }}</text>
        <text v-if="authConfig?.smsMockEnabled" class="mock-tip">开发模式验证码：123456</text>
        <view class="modal-actions">
          <button @click="passwordVisible = false">取消</button>
          <button class="primary" :loading="passwordSubmitting" @click="submitPassword">保存</button>
        </view>
      </view>
    </view>

    <!-- 解绑手机号 -->
    <view v-if="unbindVisible" class="modal-mask" @click="unbindVisible = false">
      <view class="modal" @click.stop>
        <text class="modal-title">解绑手机号</text>
        <text class="warn-tip">解绑后无法使用手机号登录，请确保已绑定其他登录方式</text>
        <view class="sms-row">
          <input v-model="unbindForm.code" class="input flex" maxlength="6" placeholder="验证码" />
          <button
            class="sms-btn"
            size="mini"
            :disabled="unbindCountdown > 0 || unbindSending"
            @click="sendUnbindCode"
          >
            {{ unbindCountdown > 0 ? `${unbindCountdown}s` : '获取验证码' }}
          </button>
        </view>
        <view class="modal-actions">
          <button @click="unbindVisible = false">取消</button>
          <button class="danger" :loading="unbindSubmitting" @click="submitUnbind">确认解绑</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import {onShow} from '@dcloudio/uni-app'
import {computed, reactive, ref} from 'vue'
import {bindMobile, fetchAuthBindings, fetchAuthConfig, fetchSmsSend, setPassword, unbindMobile} from '@/api/auth'
import type {PortalAuthConfig, PortalMemberAuthBinding} from '@/api/types'
import {isLogin} from '@/stores/member'

const AUTH_TYPE_PASSWORD = 1
const AUTH_TYPE_MOBILE = 2
const AUTH_TYPE_WECHAT = 3

const loading = ref(false)
const bindings = ref<PortalMemberAuthBinding[]>([])
const authConfig = ref<PortalAuthConfig | null>(null)

const bindVisible = ref(false)
const passwordVisible = ref(false)
const unbindVisible = ref(false)

const bindSending = ref(false)
const bindSubmitting = ref(false)
const bindCountdown = ref(0)
const passwordSending = ref(false)
const passwordSubmitting = ref(false)
const passwordCountdown = ref(0)
const unbindSending = ref(false)
const unbindSubmitting = ref(false)
const unbindCountdown = ref(0)

const bindForm = reactive({ mobile: '', code: '' })
const passwordForm = reactive({ password: '', confirmPassword: '', code: '' })
const unbindForm = reactive({ code: '' })

let bindTimer: ReturnType<typeof setInterval> | null = null
let passwordTimer: ReturnType<typeof setInterval> | null = null
let unbindTimer: ReturnType<typeof setInterval> | null = null

const hasMobileBinding = computed(() =>
  bindings.value.some((b) => b.authType === AUTH_TYPE_MOBILE)
)
const hasPasswordBinding = computed(() =>
  bindings.value.some((b) => b.authType === AUTH_TYPE_PASSWORD)
)
const boundMobile = computed(
  () => bindings.value.find((b) => b.authType === AUTH_TYPE_MOBILE)?.identifier || ''
)
const canUnbindMobile = computed(() => bindings.value.length > 1)

function authIcon(authType: number) {
  if (authType === AUTH_TYPE_PASSWORD) return '🔒'
  if (authType === AUTH_TYPE_MOBILE) return '📱'
  if (authType === AUTH_TYPE_WECHAT) return '💬'
  return '👤'
}

function maskMobile(mobile: string) {
  if (!mobile || mobile.length < 7) return mobile
  return `${mobile.slice(0, 3)}****${mobile.slice(-4)}`
}

function startCountdown(target: typeof bindCountdown, timerRef: () => ReturnType<typeof setInterval> | null, setter: (t: ReturnType<typeof setInterval> | null) => void) {
  target.value = 60
  const existing = timerRef()
  if (existing) clearInterval(existing)
  const timer = setInterval(() => {
    target.value -= 1
    if (target.value <= 0) {
      clearInterval(timer)
      setter(null)
    }
  }, 1000)
  setter(timer)
}

async function load() {
  if (!isLogin()) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  loading.value = true
  try {
    const [list, config] = await Promise.all([fetchAuthBindings(), fetchAuthConfig()])
    bindings.value = list
    authConfig.value = config
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

function openBindDialog() {
  bindForm.mobile = ''
  bindForm.code = ''
  bindVisible.value = true
}

function openPasswordDialog() {
  if (!hasMobileBinding.value) {
    uni.showToast({ title: '请先绑定手机号', icon: 'none' })
    return
  }
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
  passwordForm.code = ''
  passwordVisible.value = true
}

function openUnbindDialog() {
  unbindForm.code = ''
  unbindVisible.value = true
}

async function sendBindCode() {
  if (!/^1\d{10}$/.test(bindForm.mobile)) {
    uni.showToast({ title: '请输入正确手机号', icon: 'none' })
    return
  }
  bindSending.value = true
  try {
    await fetchSmsSend(bindForm.mobile, 'bind')
    startCountdown(bindCountdown, () => bindTimer, (t) => { bindTimer = t })
    uni.showToast({ title: '验证码已发送' })
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    bindSending.value = false
  }
}

async function submitBind() {
  if (!bindForm.mobile || !bindForm.code) {
    uni.showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  bindSubmitting.value = true
  try {
    await bindMobile(bindForm.mobile, bindForm.code)
    bindVisible.value = false
    uni.showToast({ title: '绑定成功' })
    await load()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    bindSubmitting.value = false
  }
}

async function sendPasswordCode() {
  if (!boundMobile.value) return
  passwordSending.value = true
  try {
    await fetchSmsSend(boundMobile.value, 'set_password')
    startCountdown(passwordCountdown, () => passwordTimer, (t) => { passwordTimer = t })
    uni.showToast({ title: '验证码已发送' })
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    passwordSending.value = false
  }
}

async function submitPassword() {
  if (!passwordForm.password || passwordForm.password.length < 6) {
    uni.showToast({ title: '密码至少 6 位', icon: 'none' })
    return
  }
  if (passwordForm.password !== passwordForm.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  if (!passwordForm.code) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  passwordSubmitting.value = true
  try {
    await setPassword(passwordForm.password, passwordForm.code)
    passwordVisible.value = false
    uni.showToast({ title: '密码已保存' })
    await load()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    passwordSubmitting.value = false
  }
}

async function sendUnbindCode() {
  if (!boundMobile.value) return
  unbindSending.value = true
  try {
    await fetchSmsSend(boundMobile.value, 'unbind')
    startCountdown(unbindCountdown, () => unbindTimer, (t) => { unbindTimer = t })
    uni.showToast({ title: '验证码已发送' })
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    unbindSending.value = false
  }
}

async function submitUnbind() {
  if (!unbindForm.code) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  unbindSubmitting.value = true
  try {
    await unbindMobile(unbindForm.code)
    unbindVisible.value = false
    uni.showToast({ title: '已解绑' })
    await load()
  } catch (e) {
    uni.showToast({ title: (e as Error).message, icon: 'none' })
  } finally {
    unbindSubmitting.value = false
  }
}

onShow(load)
</script>

<style scoped>
.page {
  padding: 24rpx;
  min-height: 100vh;
  background: #f5f5f5;
}
.card {
  margin-bottom: 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
}
.section-title {
  margin-bottom: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
}
.binding-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.binding-item:last-child {
  border-bottom: none;
}
.binding-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 28rpx;
}
.binding-body {
  flex: 1;
  min-width: 0;
}
.binding-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}
.binding-id,
.binding-time {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #999;
}
.tag {
  font-size: 22rpx;
  color: #999;
}
.tag.ok {
  color: #52c41a;
}
.unlink-btn {
  margin: 0;
  color: #e64545;
  background: #fff5f5;
}
.action-item {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.action-item:last-child {
  border-bottom: none;
}
.action-item.disabled {
  opacity: 0.5;
}
.action-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
}
.action-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #999;
}
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
}
.modal {
  width: 86%;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
}
.modal-title {
  display: block;
  margin-bottom: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  text-align: center;
}
.input {
  width: 100%;
  margin-bottom: 16rpx;
  padding: 16rpx 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.input.flex {
  flex: 1;
  margin-bottom: 0;
}
.sms-row {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
}
.sms-btn {
  margin: 0;
  white-space: nowrap;
  background: #e6f4ff;
  color: #1677ff;
}
.mock-tip,
.warn-tip {
  display: block;
  margin-bottom: 16rpx;
  font-size: 24rpx;
  color: #999;
}
.warn-tip {
  color: #e64545;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  margin-top: 8rpx;
}
.modal-actions button {
  margin: 0;
}
.modal-actions .primary {
  background: #1677ff;
  color: #fff;
}
.modal-actions .danger {
  background: #e64545;
  color: #fff;
}
.hint,
.hint-inline {
  padding: 40rpx 0;
  text-align: center;
  color: #999;
  font-size: 26rpx;
}
</style>
