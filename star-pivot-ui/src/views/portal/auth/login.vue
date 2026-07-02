<!-- C 端会员登录 -->
<template>
  <div class="portal-auth">
    <div class="portal-auth__visual">
      <div class="portal-auth__visual-content">
        <h2>StarPivot 商城</h2>
        <p>精选好物，品质生活</p>
        <ul class="portal-auth__features">
          <li><ArtSvgIcon icon="ri:shield-check-line" /> 正品保障</li>
          <li><ArtSvgIcon icon="ri:truck-line" /> 极速配送</li>
          <li><ArtSvgIcon icon="ri:coupon-3-line" /> 专属优惠</li>
        </ul>
      </div>
    </div>

    <div class="portal-auth__card">
      <h1 class="portal-auth__title">欢迎回来</h1>
      <p class="portal-auth__subtitle">
        登录你的会员账号，
        <RouterLink class="portal-auth__switch-link" :to="registerLink">还没有账号？去注册</RouterLink>
      </p>

      <ElTabs v-model="activeTab" class="auth-tabs">
        <ElTabPane v-if="authConfig.passwordLogin !== false" label="密码登录" name="password">
          <ElForm
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            autocomplete="off"
            @submit.prevent="handlePasswordLogin"
          >
            <input
              type="text"
              tabindex="-1"
              aria-hidden="true"
              class="portal-auth__autofill-trap"
              autocomplete="username"
            />
            <input
              type="password"
              tabindex="-1"
              aria-hidden="true"
              class="portal-auth__autofill-trap"
              autocomplete="current-password"
            />
            <ElFormItem prop="account">
              <ElInput
                v-model="loginForm.account"
                name="portal-member-account"
                autocomplete="off"
                placeholder="用户名 / 手机号"
                size="large"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:user-line" />
                </template>
              </ElInput>
            </ElFormItem>
            <ElFormItem prop="password">
              <ElInput
                v-model="loginForm.password"
                name="portal-member-password"
                type="password"
                placeholder="密码"
                size="large"
                autocomplete="new-password"
                show-password
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:lock-line" />
                </template>
              </ElInput>
            </ElFormItem>
            <ElButton type="primary" size="large" class="submit-btn" :loading="loading" native-type="submit">
              登录
            </ElButton>
          </ElForm>
        </ElTabPane>

        <ElTabPane v-if="authConfig.smsLogin !== false" label="验证码登录" name="sms">
          <ElForm
            ref="smsFormRef"
            :model="smsForm"
            :rules="smsRules"
            autocomplete="off"
            @submit.prevent="handleSmsLogin"
          >
            <ElFormItem prop="mobile">
              <ElInput
                v-model="smsForm.mobile"
                name="portal-sms-mobile"
                autocomplete="off"
                placeholder="手机号"
                maxlength="11"
                size="large"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:phone-line" />
                </template>
              </ElInput>
            </ElFormItem>
            <ElFormItem prop="code">
              <div class="sms-code-row">
                <ElInput
                  v-model="smsForm.code"
                  name="portal-sms-code"
                  autocomplete="off"
                  placeholder="验证码"
                  maxlength="6"
                  size="large"
                >
                  <template #prefix>
                    <ArtSvgIcon icon="ri:shield-keyhole-line" />
                  </template>
                </ElInput>
                <ElButton
                  type="primary"
                  plain
                  size="large"
                  class="sms-send-btn"
                  :disabled="smsCountdown > 0 || smsSending"
                  :loading="smsSending"
                  @click="handleSendSms"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                </ElButton>
              </div>
            </ElFormItem>
            <p v-if="authConfig.smsMockEnabled" class="sms-mock-tip">开发模式：验证码 {{ mockCodeHint }}</p>
            <ElButton type="primary" size="large" class="submit-btn" :loading="loading" native-type="submit">
              登录
            </ElButton>
          </ElForm>
        </ElTabPane>
      </ElTabs>

      <div v-if="authConfig.wechatLogin" class="oauth-section">
        <div class="oauth-divider"><span>其他登录方式</span></div>
        <button type="button" class="oauth-btn oauth-btn--wechat" :disabled="wechatLoading" @click="handleWechatLogin">
          <ArtSvgIcon icon="ri:wechat-fill" />
          微信登录
        </button>
      </div>

      <button type="button" class="back-link" @click="router.push('/portal')">
        <ArtSvgIcon icon="ri:arrow-left-line" />
        返回商城首页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import {
  fetchPortalAuthConfig,
  fetchPortalPasswordLogin,
  fetchPortalSmsLogin,
  fetchPortalSmsSend,
  startPortalWechatOAuth
} from '@/api/portal/auth'
import type {PortalAuthConfig} from '@/api/portal/types'
import {usePortalMemberStore} from '@/store/modules/portal-member'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

defineOptions({ name: 'PortalLogin' })

  const route = useRoute()
  const router = useRouter()
  const portalStore = usePortalMemberStore()

  const authConfig = ref<PortalAuthConfig>({
    passwordLogin: true,
    smsLogin: true,
    wechatLogin: false,
    qqLogin: false,
    smsMockEnabled: false,
    captchaRequired: false
  })
  const mockCodeHint = '123456'

  const activeTab = ref('password')
  const loading = ref(false)
  const wechatLoading = ref(false)
  const smsSending = ref(false)
  const smsCountdown = ref(0)
  let smsTimer: ReturnType<typeof setInterval> | null = null

  const loginFormRef = ref<FormInstance>()
  const smsFormRef = ref<FormInstance>()

  const loginForm = reactive({ account: '', password: '' })
  const smsForm = reactive({ mobile: '', code: '' })

  const mobilePattern = /^1[3-9]\d{9}$/

  const registerLink = computed(() => {
    const redirect = route.query.redirect as string | undefined
    return redirect ? { path: '/portal/register', query: { redirect } } : '/portal/register'
  })

  const loginRules: FormRules = {
    account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }

  const smsRules: FormRules = {
    mobile: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: mobilePattern, message: '手机号格式不正确', trigger: 'blur' }
    ],
    code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
  }

  function redirectAfterAuth() {
    const redirect = (route.query.redirect as string) || '/portal'
    router.replace(redirect)
  }

  async function handleWechatLogin() {
    wechatLoading.value = true
    try {
      const redirect = (route.query.redirect as string) || '/portal'
      await startPortalWechatOAuth({ mode: 'login', redirect })
    } finally {
      wechatLoading.value = false
    }
  }

  function startSmsCountdown(seconds = 60) {
    smsCountdown.value = seconds
    if (smsTimer) clearInterval(smsTimer)
    smsTimer = setInterval(() => {
      smsCountdown.value -= 1
      if (smsCountdown.value <= 0 && smsTimer) {
        clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  }

  async function handleSendSms() {
    const valid = await smsFormRef.value?.validateField('mobile').catch(() => false)
    if (!valid) return
    smsSending.value = true
    try {
      const res = await fetchPortalSmsSend({ mobile: smsForm.mobile, scene: 'login' })
      ElMessage.success('验证码已发送')
      startSmsCountdown(res.expireSeconds >= 60 ? 60 : res.expireSeconds)
    } finally {
      smsSending.value = false
    }
  }

  async function handlePasswordLogin() {
    await loginFormRef.value?.validate()
    loading.value = true
    try {
      const result = await fetchPortalPasswordLogin(loginForm)
      portalStore.setLogin(result)
      ElMessage.success('登录成功')
      redirectAfterAuth()
    } finally {
      loading.value = false
    }
  }

  async function handleSmsLogin() {
    await smsFormRef.value?.validate()
    loading.value = true
    try {
      const result = await fetchPortalSmsLogin(smsForm)
      portalStore.setLogin(result)
      ElMessage.success('登录成功')
      redirectAfterAuth()
    } finally {
      loading.value = false
    }
  }

  onMounted(async () => {
    if (portalStore.isLogin) {
      redirectAfterAuth()
      return
    }
    const username = route.query.username as string | undefined
    if (username) {
      loginForm.account = username
    }
    try {
      authConfig.value = await fetchPortalAuthConfig()
      if (authConfig.value.passwordLogin === false && authConfig.value.smsLogin !== false) {
        activeTab.value = 'sms'
      }
    } catch {
      // 配置加载失败时使用默认开关
    }
  })

  onUnmounted(() => {
    if (smsTimer) clearInterval(smsTimer)
  })
</script>

<style scoped lang="scss">
  @use './auth-shell.scss';
</style>
