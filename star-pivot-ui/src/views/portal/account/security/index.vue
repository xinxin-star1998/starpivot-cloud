<!-- C 端账号安全 -->
<template>
  <div v-loading="loading" class="portal-security">
    <PortalPageHeader title="账号安全" subtitle="管理登录方式，保障账号安全" />

    <div class="security-card">
      <div class="security-card__head">
        <h2>已绑定登录方式</h2>
        <p>同一账号可绑定多种方式，任意一种均可登录</p>
      </div>

      <div v-if="bindings.length" class="binding-list">
        <div v-for="item in bindings" :key="`${item.authType}-${item.identifier}`" class="binding-item">
          <div class="binding-item__icon" :class="`binding-item__icon--${item.authType}`">
            <ArtSvgIcon :icon="authIcon(item.authType)" />
          </div>
          <div class="binding-item__body">
            <div class="binding-item__title">{{ item.authTypeLabel }}</div>
            <div class="binding-item__identifier">{{ item.maskedIdentifier || item.identifier }}</div>
            <div v-if="item.bindTime" class="binding-item__time">绑定于 {{ item.bindTime }}</div>
          </div>
          <div class="binding-item__action">
            <ElButton
              v-if="item.authType === AUTH_TYPE_MOBILE && canUnbindMobile"
              link
              type="danger"
              @click="openUnbindDialog"
            >
              解绑
            </ElButton>
            <ElButton
              v-else-if="item.authType === AUTH_TYPE_WECHAT && canUnbindWechat"
              link
              type="danger"
              :loading="wechatUnbinding"
              @click="handleUnbindWechat"
            >
              解绑
            </ElButton>
            <ElTag v-else-if="item.authType === AUTH_TYPE_PASSWORD" type="success" size="small">已启用</ElTag>
            <ElTag v-else type="info" size="small">已绑定</ElTag>
          </div>
        </div>
      </div>

      <ElEmpty v-else description="暂无绑定记录" :image-size="80" />
    </div>

    <div class="security-actions">
      <button v-if="!hasMobileBinding" type="button" class="action-card" @click="openBindDialog">
        <ArtSvgIcon icon="ri:phone-line" class="action-card__icon" />
        <div class="action-card__text">
          <strong>绑定手机号</strong>
          <span>用于验证码登录与安全验证</span>
        </div>
        <ArtSvgIcon icon="ri:arrow-right-s-line" class="action-card__arrow" />
      </button>

      <button
        v-if="authConfig.wechatLogin && !hasWechatBinding"
        type="button"
        class="action-card"
        :disabled="wechatBinding"
        @click="handleBindWechat"
      >
        <ArtSvgIcon icon="ri:wechat-fill" class="action-card__icon action-card__icon--wechat" />
        <div class="action-card__text">
          <strong>绑定微信</strong>
          <span>绑定后可使用微信一键登录</span>
        </div>
        <ArtSvgIcon icon="ri:arrow-right-s-line" class="action-card__arrow" />
      </button>

      <button
        type="button"
        class="action-card"
        :class="{ 'action-card--disabled': !hasMobileBinding }"
        @click="openPasswordDialog"
      >
        <ArtSvgIcon icon="ri:lock-password-line" class="action-card__icon" />
        <div class="action-card__text">
          <strong>{{ hasPasswordBinding ? '修改登录密码' : '设置登录密码' }}</strong>
          <span>{{ hasMobileBinding ? '需已绑定手机号接收验证码' : '请先绑定手机号' }}</span>
        </div>
        <ArtSvgIcon icon="ri:arrow-right-s-line" class="action-card__arrow" />
      </button>
    </div>

    <!-- 绑定手机号 -->
    <ElDialog v-model="bindVisible" title="绑定手机号" width="420px" destroy-on-close @closed="resetBindForm">
      <ElForm ref="bindFormRef" :model="bindForm" :rules="bindRules" label-position="top">
        <ElFormItem label="手机号" prop="mobile">
          <ElInput v-model="bindForm.mobile" maxlength="11" placeholder="请输入手机号" />
        </ElFormItem>
        <ElFormItem label="验证码" prop="code">
          <div class="sms-code-row">
            <ElInput v-model="bindForm.code" maxlength="6" placeholder="短信验证码" />
            <ElButton
              type="primary"
              plain
              :disabled="bindCountdown > 0 || bindSending"
              :loading="bindSending"
              @click="sendBindCode"
            >
              {{ bindCountdown > 0 ? `${bindCountdown}s` : '获取验证码' }}
            </ElButton>
          </div>
        </ElFormItem>
        <p v-if="authConfig.smsMockEnabled" class="sms-mock-tip">开发模式验证码：123456</p>
      </ElForm>
      <template #footer>
        <ElButton @click="bindVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="bindSubmitting" @click="submitBind">确认绑定</ElButton>
      </template>
    </ElDialog>

    <!-- 设置密码 -->
    <ElDialog v-model="passwordVisible" title="设置登录密码" width="420px" destroy-on-close @closed="resetPasswordForm">
      <ElForm ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
        <ElFormItem label="新密码" prop="password">
          <ElInput v-model="passwordForm.password" type="password" show-password placeholder="6-32 位" />
        </ElFormItem>
        <ElFormItem label="确认密码" prop="confirmPassword">
          <ElInput v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入密码" />
        </ElFormItem>
        <ElFormItem label="验证码" prop="code">
          <div class="sms-code-row">
            <ElInput v-model="passwordForm.code" maxlength="6" placeholder="已绑定手机号验证码" />
            <ElButton
              type="primary"
              plain
              :disabled="passwordCountdown > 0 || passwordSending"
              :loading="passwordSending"
              @click="sendPasswordCode"
            >
              {{ passwordCountdown > 0 ? `${passwordCountdown}s` : '获取验证码' }}
            </ElButton>
          </div>
        </ElFormItem>
        <p v-if="boundMobile" class="sms-mobile-tip">验证码将发送至 {{ maskMobile(boundMobile) }}</p>
        <p v-if="authConfig.smsMockEnabled" class="sms-mock-tip">开发模式验证码：123456</p>
      </ElForm>
      <template #footer>
        <ElButton @click="passwordVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="passwordSubmitting" @click="submitPassword">保存</ElButton>
      </template>
    </ElDialog>

    <!-- 解绑手机号 -->
    <ElDialog v-model="unbindVisible" title="解绑手机号" width="420px" destroy-on-close @closed="resetUnbindForm">
      <ElAlert type="warning" :closable="false" show-icon class="unbind-alert">
        解绑后将无法使用该手机号登录，请确保至少保留一种其他登录方式。
      </ElAlert>
      <ElForm ref="unbindFormRef" :model="unbindForm" :rules="unbindRules" label-position="top">
        <ElFormItem label="验证码" prop="code">
          <div class="sms-code-row">
            <ElInput v-model="unbindForm.code" maxlength="6" placeholder="已绑定手机号验证码" />
            <ElButton
              type="primary"
              plain
              :disabled="unbindCountdown > 0 || unbindSending"
              :loading="unbindSending"
              @click="sendUnbindCode"
            >
              {{ unbindCountdown > 0 ? `${unbindCountdown}s` : '获取验证码' }}
            </ElButton>
          </div>
        </ElFormItem>
        <p v-if="boundMobile" class="sms-mobile-tip">验证码将发送至 {{ maskMobile(boundMobile) }}</p>
        <p v-if="authConfig.smsMockEnabled" class="sms-mock-tip">开发模式验证码：123456</p>
      </ElForm>
      <template #footer>
        <ElButton @click="unbindVisible = false">取消</ElButton>
        <ElButton type="danger" :loading="unbindSubmitting" @click="submitUnbind">确认解绑</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessageBox} from 'element-plus'
import {
  fetchPortalAuthBindings,
  fetchPortalAuthConfig,
  fetchPortalBindMobile,
  fetchPortalSetPassword,
  fetchPortalSmsSend,
  fetchPortalUnbindMobile,
  fetchPortalUnbindWechat,
  startPortalWechatOAuth
} from '@/api/portal/auth'
import type {PortalAuthConfig, PortalMemberAuthBinding} from '@/api/portal/types'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import PortalPageHeader from '../../components/portal-page-header.vue'

defineOptions({ name: 'PortalAccountSecurity' })

  const AUTH_TYPE_PASSWORD = 1
  const AUTH_TYPE_MOBILE = 2
  const AUTH_TYPE_WECHAT = 3

  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const wechatBinding = ref(false)
  const wechatUnbinding = ref(false)
  const bindings = ref<PortalMemberAuthBinding[]>([])
  const authConfig = ref<PortalAuthConfig>({
    passwordLogin: true,
    smsLogin: true,
    wechatLogin: false,
    qqLogin: false,
    smsMockEnabled: false,
    captchaRequired: false
  })

  const mobilePattern = /^1[3-9]\d{9}$/

  const hasMobileBinding = computed(() => bindings.value.some((b) => b.authType === AUTH_TYPE_MOBILE))
  const hasPasswordBinding = computed(() => bindings.value.some((b) => b.authType === AUTH_TYPE_PASSWORD))
  const hasWechatBinding = computed(() => bindings.value.some((b) => b.authType === AUTH_TYPE_WECHAT))
  const canUnbindMobile = computed(() => hasMobileBinding.value && bindings.value.length > 1)
  const canUnbindWechat = computed(() => hasWechatBinding.value && bindings.value.length > 1)
  const boundMobile = computed(
    () => bindings.value.find((b) => b.authType === AUTH_TYPE_MOBILE)?.identifier || ''
  )

  function authIcon(authType: number) {
    if (authType === AUTH_TYPE_PASSWORD) return 'ri:lock-line'
    if (authType === AUTH_TYPE_MOBILE) return 'ri:phone-line'
    if (authType === 3) return 'ri:wechat-line'
    if (authType === 4) return 'ri:qq-line'
    return 'ri:shield-user-line'
  }

  function maskMobile(mobile: string) {
    if (mobile.length < 11) return mobile
    return `${mobile.slice(0, 3)}****${mobile.slice(7)}`
  }

  async function loadBindings() {
    bindings.value = await fetchPortalAuthBindings()
  }

  async function loadPage() {
    loading.value = true
    try {
      const [config] = await Promise.all([fetchPortalAuthConfig(), loadBindings()])
      authConfig.value = config
    } finally {
      loading.value = false
    }
  }

  async function handleBindWechat() {
    wechatBinding.value = true
    try {
      await startPortalWechatOAuth({ mode: 'bind', redirect: '/portal/account/security' })
    } finally {
      wechatBinding.value = false
    }
  }

  async function handleUnbindWechat() {
    await ElMessageBox.confirm('解绑后将无法使用该微信登录此账号，是否继续？', '解绑微信', {
      type: 'warning',
      confirmButtonText: '确认解绑',
      cancelButtonText: '取消'
    })
    wechatUnbinding.value = true
    try {
      await fetchPortalUnbindWechat()
      await loadBindings()
    } finally {
      wechatUnbinding.value = false
    }
  }

  // --- 倒计时工具 ---
  function useSmsCountdown() {
    const countdown = ref(0)
    let timer: ReturnType<typeof setInterval> | null = null

    function start(seconds = 60) {
      countdown.value = seconds
      if (timer) clearInterval(timer)
      timer = setInterval(() => {
        countdown.value -= 1
        if (countdown.value <= 0 && timer) {
          clearInterval(timer)
          timer = null
        }
      }, 1000)
    }

    function stop() {
      if (timer) clearInterval(timer)
      timer = null
      countdown.value = 0
    }

    onUnmounted(stop)
    return { countdown, start, stop }
  }

  // --- 绑定手机 ---
  const bindVisible = ref(false)
  const bindSubmitting = ref(false)
  const bindSending = ref(false)
  const bindFormRef = ref<FormInstance>()
  const bindForm = reactive({ mobile: '', code: '' })
  const { countdown: bindCountdown, start: startBindCountdown } = useSmsCountdown()

  const bindRules: FormRules = {
    mobile: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: mobilePattern, message: '手机号格式不正确', trigger: 'blur' }
    ],
    code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
  }

  function openBindDialog() {
    bindVisible.value = true
  }

  function resetBindForm() {
    bindForm.mobile = ''
    bindForm.code = ''
  }

  async function sendBindCode() {
    const valid = await bindFormRef.value?.validateField('mobile').catch(() => false)
    if (!valid) return
    bindSending.value = true
    try {
      const res = await fetchPortalSmsSend({ mobile: bindForm.mobile, scene: 'bind' })
      startBindCountdown(res.expireSeconds >= 60 ? 60 : res.expireSeconds)
    } finally {
      bindSending.value = false
    }
  }

  async function submitBind() {
    await bindFormRef.value?.validate()
    bindSubmitting.value = true
    try {
      await fetchPortalBindMobile({ mobile: bindForm.mobile, code: bindForm.code })
      bindVisible.value = false
      await loadBindings()
    } finally {
      bindSubmitting.value = false
    }
  }

  // --- 设置密码 ---
  const passwordVisible = ref(false)
  const passwordSubmitting = ref(false)
  const passwordSending = ref(false)
  const passwordFormRef = ref<FormInstance>()
  const passwordForm = reactive({ password: '', confirmPassword: '', code: '' })
  const { countdown: passwordCountdown, start: startPasswordCountdown } = useSmsCountdown()

  const passwordRules: FormRules = {
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请确认密码', trigger: 'blur' },
      {
        validator: (_rule, value, callback) => {
          if (value !== passwordForm.password) callback(new Error('两次密码不一致'))
          else callback()
        },
        trigger: 'blur'
      }
    ],
    code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
  }

  function openPasswordDialog() {
    if (!hasMobileBinding.value) return
    passwordVisible.value = true
  }

  function resetPasswordForm() {
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
    passwordForm.code = ''
  }

  async function sendPasswordCode() {
    if (!boundMobile.value) return
    passwordSending.value = true
    try {
      const res = await fetchPortalSmsSend({ mobile: boundMobile.value, scene: 'set_password' })
      startPasswordCountdown(res.expireSeconds >= 60 ? 60 : res.expireSeconds)
    } finally {
      passwordSending.value = false
    }
  }

  async function submitPassword() {
    await passwordFormRef.value?.validate()
    passwordSubmitting.value = true
    try {
      await fetchPortalSetPassword({ password: passwordForm.password, code: passwordForm.code })
      passwordVisible.value = false
      await loadBindings()
    } finally {
      passwordSubmitting.value = false
    }
  }

  // --- 解绑手机 ---
  const unbindVisible = ref(false)
  const unbindSubmitting = ref(false)
  const unbindSending = ref(false)
  const unbindFormRef = ref<FormInstance>()
  const unbindForm = reactive({ code: '' })
  const { countdown: unbindCountdown, start: startUnbindCountdown } = useSmsCountdown()

  const unbindRules: FormRules = {
    code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
  }

  function openUnbindDialog() {
    unbindVisible.value = true
  }

  function resetUnbindForm() {
    unbindForm.code = ''
  }

  async function sendUnbindCode() {
    if (!boundMobile.value) return
    unbindSending.value = true
    try {
      const res = await fetchPortalSmsSend({ mobile: boundMobile.value, scene: 'unbind' })
      startUnbindCountdown(res.expireSeconds >= 60 ? 60 : res.expireSeconds)
    } finally {
      unbindSending.value = false
    }
  }

  async function submitUnbind() {
    await unbindFormRef.value?.validate()
    unbindSubmitting.value = true
    try {
      await fetchPortalUnbindMobile(unbindForm.code)
      unbindVisible.value = false
      await loadBindings()
    } finally {
      unbindSubmitting.value = false
    }
  }

  onMounted(() => {
    if (!requireLogin()) return
    loadPage()
  })
</script>

<style scoped lang="scss">

  .security-card {
    background: var(--portal-bg-elevated);
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius-lg);
    box-shadow: var(--portal-shadow-sm);
    overflow: hidden;
    margin-bottom: 16px;

    &__head {
      padding: 20px 24px 12px;
      border-bottom: 1px solid var(--portal-border);

      h2 {
        margin: 0 0 6px;
        font-size: 16px;
        font-weight: 700;
        color: var(--portal-text);
      }

      p {
        margin: 0;
        font-size: 13px;
        color: var(--portal-text-secondary);
      }
    }
  }

  .binding-list {
    padding: 8px 0;
  }

  .binding-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 24px;
    border-bottom: 1px solid var(--portal-border);

    &:last-child {
      border-bottom: none;
    }

    &__icon {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
      flex-shrink: 0;

      &--1 {
        background: #eef2ff;
        color: #4f6ef7;
      }

      &--2 {
        background: #ecfdf5;
        color: #10b981;
      }

      &--3 {
        background: #ecfdf5;
        color: #07c160;
      }

      &--4 {
        background: #eff6ff;
        color: #3b82f6;
      }
    }

    &__body {
      flex: 1;
      min-width: 0;
    }

    &__title {
      font-size: 15px;
      font-weight: 600;
      color: var(--portal-text);
    }

    &__identifier {
      margin-top: 2px;
      font-size: 13px;
      color: var(--portal-text-secondary);
    }

    &__time {
      margin-top: 4px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__action {
      flex-shrink: 0;
    }
  }

  .security-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .action-card {
    display: flex;
    align-items: center;
    gap: 14px;
    width: 100%;
    padding: 18px 20px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius-lg);
    background: var(--portal-bg-elevated);
    box-shadow: var(--portal-shadow-sm);
    cursor: pointer;
    text-align: left;
    transition: all var(--portal-transition);

    &:hover:not(.action-card--disabled) {
      border-color: var(--portal-primary);
      box-shadow: var(--portal-shadow);
    }

    &--disabled {
      opacity: 0.55;
      cursor: not-allowed;
    }

    &__icon {
      font-size: 28px;
      color: var(--portal-primary);
      flex-shrink: 0;

      &--wechat {
        color: #07c160;
      }
    }

    &__text {
      flex: 1;
      min-width: 0;

      strong {
        display: block;
        font-size: 15px;
        color: var(--portal-text);
        margin-bottom: 4px;
      }

      span {
        font-size: 12px;
        color: var(--portal-text-secondary);
      }
    }

    &__arrow {
      font-size: 20px;
      color: var(--portal-text-muted);
      flex-shrink: 0;
    }
  }

  .sms-code-row {
    display: flex;
    gap: 8px;
    width: 100%;

    .el-input {
      flex: 1;
    }

    .el-button {
      flex-shrink: 0;
      min-width: 108px;
    }
  }

  .sms-mock-tip,
  .sms-mobile-tip {
    margin: 0 0 8px;
    font-size: 12px;
    color: var(--portal-text-muted);
  }

  .unbind-alert {
    margin-bottom: 16px;
  }
</style>
