<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <div class="form-header">
            <h3 class="title">{{ t('forgetPassword.title') }}</h3>
            <p class="sub-title">{{ t('forgetPassword.subTitle') }}</p>
          </div>

          <ElForm
            ref="formRef"
            :model="formData"
            :rules="rules"
            class="form-content"
            label-position="top"
          >
            <ElFormItem prop="username">
              <ElInput
                class="custom-height"
                v-model.trim="formData.username"
                :placeholder="t('forgetPassword.placeholder')"
                clearable
              >
                <template #prefix>
                  <ArtSvgIcon
                    icon="ri:user-line"
                    class="text-lg transition-colors"
                    :class="isDark ? 'text-g-500' : 'text-g-400'"
                  />
                </template>
              </ElInput>
            </ElFormItem>

            <ElFormItem prop="password">
              <ElInput
                v-model.trim="formData.password"
                :placeholder="t('forgetPassword.passwordPlaceholder')"
                class="custom-height"
                show-password
                type="password"
              >
                <template #prefix>
                  <ArtSvgIcon
                    :class="isDark ? 'text-g-500' : 'text-g-400'"
                    class="text-lg transition-colors"
                    icon="ri:lock-line"
                  />
                </template>
              </ElInput>
            </ElFormItem>

            <ElFormItem prop="confirmPassword">
              <ElInput
                v-model.trim="formData.confirmPassword"
                :placeholder="t('forgetPassword.confirmPasswordPlaceholder')"
                class="custom-height"
                show-password
                type="password"
                @keyup.enter="submit"
              >
                <template #prefix>
                  <ArtSvgIcon
                    :class="isDark ? 'text-g-500' : 'text-g-400'"
                    class="text-lg transition-colors"
                    icon="ri:lock-2-line"
                  />
                </template>
              </ElInput>
            </ElFormItem>

            <ElFormItem prop="captcha">
              <div class="flex w-full gap-2">
                <ElInput
                  v-model.trim="formData.captcha"
                  :placeholder="t('login.placeholder.captcha')"
                  class="custom-height flex-1"
                  @keyup.enter="submit"
                />
                <img
                  v-if="captchaImage"
                  :src="captchaImage"
                  alt="captcha"
                  class="h-[40px] cursor-pointer rounded border border-g-300"
                  @click="loadCaptcha"
                />
              </div>
            </ElFormItem>

            <ElButton
              class="submit-btn custom-height"
              type="primary"
              @click="submit"
              :loading="loading"
              v-ripple
            >
              {{ t('forgetPassword.submitBtnText') }}
            </ElButton>

            <ElButton class="back-btn custom-height" plain @click="toLogin">
              {{ t('forgetPassword.backBtnText') }}
            </ElButton>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { useI18n } from 'vue-i18n'
  import {
    fetchCaptcha,
    fetchForgetPasswordEnabled,
    fetchForgotPassword,
    fetchVerifyCaptcha
  } from '@/api/auth'
  import { useSettingStore } from '@/store/modules/setting'

  defineOptions({ name: 'ForgetPassword' })

  const { t } = useI18n()
  const router = useRouter()
  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const captchaImage = ref('')
  const captchaToken = ref('')

  const formData = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    captcha: ''
  })

  const rules: FormRules = {
    username: [{ required: true, message: () => t('forgetPassword.placeholder'), trigger: 'blur' }],
    password: [
      { required: true, message: () => t('forgetPassword.passwordPlaceholder'), trigger: 'blur' },
      { min: 6, message: () => t('register.rule.passwordLength'), trigger: 'blur' }
    ],
    confirmPassword: [
      {
        required: true,
        message: () => t('register.rule.confirmPasswordRequired'),
        trigger: 'blur'
      },
      {
        validator: (_rule, value, callback) => {
          if (value !== formData.password) {
            callback(new Error(t('register.rule.passwordMismatch')))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ],
    captcha: [{ required: true, message: () => t('login.placeholder.captcha'), trigger: 'blur' }]
  }

  const loadCaptcha = async () => {
    try {
      const res = await fetchCaptcha('forget-password')
      captchaToken.value = res.captchaToken
      captchaImage.value = res.captchaImage
    } catch {
      captchaImage.value = ''
    }
  }

  const submit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
      if (!valid) return
      loading.value = true
      try {
        const enabled = await fetchForgetPasswordEnabled()
        if (!enabled) {
          ElMessage.warning('当前未开放忘记密码功能')
          return
        }
        const verifyRes = await fetchVerifyCaptcha({
          captchaToken: captchaToken.value,
          code: formData.captcha,
          scene: 'forget-password'
        })
        await fetchForgotPassword({
          username: formData.username,
          password: formData.password,
          captchaProof: verifyRes.captchaProof
        })
        ElMessage.success('密码重置成功，请使用新密码登录')
        router.push({ name: 'Login' })
      } catch (error: any) {
        ElMessage.error(error?.message || '重置失败，请稍后重试')
        await loadCaptcha()
        formData.captcha = ''
      } finally {
        loading.value = false
      }
    })
  }

  const toLogin = () => {
    router.push({ name: 'Login' })
  }

  onMounted(() => {
    loadCaptcha()
  })
</script>

<style scoped>
  @import '../login/style.css';
</style>
