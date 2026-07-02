<!-- C 端会员注册 -->
<template>
  <div class="portal-auth">
    <div class="portal-auth__visual">
      <div class="portal-auth__visual-content">
        <h2>加入 StarPivot</h2>
        <p>注册即享新人专属优惠</p>
        <ul class="portal-auth__features">
          <li><ArtSvgIcon icon="ri:gift-line" /> 新人礼包</li>
          <li><ArtSvgIcon icon="ri:vip-crown-line" /> 会员积分</li>
          <li><ArtSvgIcon icon="ri:shield-check-line" /> 安全账号体系</li>
        </ul>
      </div>
    </div>

    <div class="portal-auth__card">
      <h1 class="portal-auth__title">创建账号</h1>
      <p class="portal-auth__subtitle">
        选择一种方式完成注册，
        <RouterLink class="portal-auth__switch-link" :to="loginLink">已有账号？去登录</RouterLink>
      </p>

      <div v-if="authConfig.wechatLogin" class="oauth-section oauth-section--primary">
        <button
          type="button"
          class="oauth-btn oauth-btn--wechat"
          :disabled="wechatLoading"
          @click="handleWechatRegister"
        >
          <ArtSvgIcon icon="ri:wechat-fill" />
          微信快捷注册
        </button>
      </div>

      <div v-if="authConfig.wechatLogin" class="oauth-divider"><span>或使用以下方式</span></div>

      <ElTabs v-model="activeTab" class="auth-tabs">
        <ElTabPane label="账号注册" name="account">
          <ElForm
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            autocomplete="off"
            @submit.prevent="handleRegister"
          >
            <ElFormItem prop="username">
              <ElInput
                v-model="registerForm.username"
                name="portal-reg-username"
                autocomplete="off"
                placeholder="用户名"
                size="large"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:user-line" />
                </template>
              </ElInput>
            </ElFormItem>
            <ElFormItem prop="mobile">
              <ElInput
                v-model="registerForm.mobile"
                name="portal-reg-mobile"
                autocomplete="tel"
                placeholder="手机号（选填）"
                size="large"
              >
                <template #prefix>
                  <ArtSvgIcon icon="ri:phone-line" />
                </template>
              </ElInput>
            </ElFormItem>
            <ElFormItem prop="password">
              <ElInput
                v-model="registerForm.password"
                name="portal-reg-password"
                type="password"
                placeholder="密码（6-32位）"
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
              注册
            </ElButton>
          </ElForm>
        </ElTabPane>

        <ElTabPane v-if="authConfig.smsLogin !== false" label="手机注册" name="sms">
          <ElForm
            ref="smsFormRef"
            :model="smsForm"
            :rules="smsRules"
            autocomplete="off"
            @submit.prevent="handleSmsRegister"
          >
            <ElFormItem prop="mobile">
              <ElInput
                v-model="smsForm.mobile"
                name="portal-reg-sms-mobile"
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
                  name="portal-reg-sms-code"
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
              注册并登录
            </ElButton>
          </ElForm>
        </ElTabPane>
      </ElTabs>

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
  fetchPortalSmsRegister,
  fetchPortalSmsSend,
  startPortalWechatOAuth
} from '@/api/portal/auth'
import {fetchPortalRegister} from '@/api/portal/member'
import type {PortalAuthConfig} from '@/api/portal/types'
import {usePortalMemberStore} from '@/store/modules/portal-member'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

defineOptions({ name: 'PortalRegister' })

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

  const activeTab = ref('account')
  const loading = ref(false)
  const wechatLoading = ref(false)
  const smsSending = ref(false)
  const smsCountdown = ref(0)
  let smsTimer: ReturnType<typeof setInterval> | null = null

  const registerFormRef = ref<FormInstance>()
  const smsFormRef = ref<FormInstance>()

  const registerForm = reactive({ username: '', mobile: '', password: '' })
  const smsForm = reactive({ mobile: '', code: '' })

  const mobilePattern = /^1[3-9]\d{9}$/

  const loginLink = computed(() => {
    const redirect = route.query.redirect as string | undefined
    return redirect ? { path: '/portal/login', query: { redirect } } : '/portal/login'
  })

  const registerRules: FormRules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
    ]
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

  async function handleWechatRegister() {
    wechatLoading.value = true
    try {
      const redirect = (route.query.redirect as string) || '/portal'
      await startPortalWechatOAuth({ mode: 'register', redirect })
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
      const res = await fetchPortalSmsSend({ mobile: smsForm.mobile, scene: 'register' })
      ElMessage.success('验证码已发送')
      startSmsCountdown(res.expireSeconds >= 60 ? 60 : res.expireSeconds)
    } finally {
      smsSending.value = false
    }
  }

  async function handleRegister() {
    await registerFormRef.value?.validate()
    loading.value = true
    try {
      await fetchPortalRegister(registerForm)
      ElMessage.success('注册成功，请登录')
      const redirect = route.query.redirect as string | undefined
      await router.push({
        path: '/portal/login',
        query: {
          ...(redirect ? { redirect } : {}),
          username: registerForm.username
        }
      })
    } finally {
      loading.value = false
    }
  }

  async function handleSmsRegister() {
    await smsFormRef.value?.validate()
    loading.value = true
    try {
      const result = await fetchPortalSmsRegister(smsForm)
      portalStore.setLogin(result)
      ElMessage.success('注册成功')
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
    try {
      authConfig.value = await fetchPortalAuthConfig()
      if (authConfig.value.wechatLogin) {
        activeTab.value = 'account'
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
