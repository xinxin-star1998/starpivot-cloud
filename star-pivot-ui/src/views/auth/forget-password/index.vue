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
              <div class="captcha-container">
                <ElInput
                  v-model.trim="formData.captcha"
                  :placeholder="t('login.placeholder.captcha')"
                  class="captcha-input custom-height"
                  @keyup.enter="submit"
                  clearable
                >
                  <template #prefix>
                    <ArtSvgIcon
                      icon="ri:shield-check-line"
                      class="text-lg transition-colors"
                      :class="isDark ? 'text-g-500' : 'text-g-400'"
                    />
                  </template>
                </ElInput>
                <CaptchaImage
                  :image="captchaImage"
                  :loading="loadingCaptcha"
                  @refresh="refreshCaptcha(clearCaptchaInput)"
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
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import {useI18n} from 'vue-i18n'
import {fetchForgetPasswordEnabled, fetchForgotPassword} from '@/api/auth'
import {useCaptcha} from '@/hooks'
import {useSettingStore} from '@/store/modules/setting'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import CaptchaImage from '@/components/core/views/login/CaptchaImage.vue'

defineOptions({ name: 'ForgetPassword' })

  const { t } = useI18n()
  const router = useRouter()
  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const { captchaToken, captchaImage, loadingCaptcha, refreshCaptcha } = useCaptcha('forget-password')

  const clearCaptchaInput = () => {
    formData.captcha = ''
  }

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
        await fetchForgotPassword({
          username: formData.username,
          password: formData.password,
          captchaToken: captchaToken.value,
          captcha: formData.captcha
        })
        ElMessage.success('密码重置成功，请使用新密码登录')
        router.push({ name: 'Login' })
      } catch (error: any) {
        ElMessage.error(error?.message || '重置失败，请稍后重试')
        await refreshCaptcha(clearCaptchaInput)
      } finally {
        loading.value = false
      }
    })
  }

  const toLogin = () => {
    router.push({ name: 'Login' })
  }

  onMounted(() => {
    refreshCaptcha(clearCaptchaInput)
  })
</script>

<style scoped>
  @import '../login/style.css';
</style>
