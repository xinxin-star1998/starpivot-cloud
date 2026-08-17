<!-- 注册页面 -->
<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <div class="form-header">
            <h3 class="title">{{ t('register.title') }}</h3>
            <p class="sub-title">{{ t('register.subTitle') }}</p>
          </div>

          <ElForm
            class="form-content"
            ref="formRef"
            :model="formData"
            :rules="rules"
            label-position="top"
            :key="formKey"
            autocomplete="off"
          >
            <ElFormItem prop="username">
              <ElInput
                class="custom-height"
                v-model.trim="formData.username"
                :placeholder="t('register.placeholder.username')"
                autocomplete="off"
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
                class="custom-height"
                v-model.trim="formData.password"
                :placeholder="t('register.placeholder.password')"
                type="password"
                autocomplete="new-password"
                show-password
              >
                <template #prefix>
                  <ArtSvgIcon
                    icon="ri:lock-line"
                    class="text-lg transition-colors"
                    :class="isDark ? 'text-g-500' : 'text-g-400'"
                  />
                </template>
              </ElInput>
            </ElFormItem>

            <ElFormItem prop="confirmPassword">
              <ElInput
                class="custom-height"
                v-model.trim="formData.confirmPassword"
                :placeholder="t('register.placeholder.confirmPassword')"
                type="password"
                autocomplete="new-password"
                @keyup.enter="register"
                show-password
              >
                <template #prefix>
                  <ArtSvgIcon
                    icon="ri:lock-2-line"
                    class="text-lg transition-colors"
                    :class="isDark ? 'text-g-500' : 'text-g-400'"
                  />
                </template>
              </ElInput>
            </ElFormItem>

            <ElFormItem prop="agreement" class="agreement-item">
              <ElCheckbox v-model="formData.agreement" class="agreement-checkbox">
                {{ t('register.agreeText') }}
                <RouterLink class="privacy-link" to="/privacy-policy">
                  {{ t('register.privacyPolicy') }}
                </RouterLink>
              </ElCheckbox>
            </ElFormItem>

            <ElButton
              class="submit-btn custom-height"
              type="primary"
              @click="register"
              :loading="loading"
              v-ripple
            >
              {{ t('register.submitBtnText') }}
            </ElButton>

            <div class="form-footer">
              <span class="footer-text">{{ t('register.hasAccount') }}</span>
              <RouterLink class="login-link" :to="{ name: 'Login' }">
                {{ t('register.toLogin') }}
              </RouterLink>
            </div>
          </ElForm>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchRegister } from '@/api/auth'
  import { isRegisterEnabled } from '@/utils/auth/register-config'
  import { useSettingStore } from '@/store/modules/setting'

  defineOptions({ name: 'Register' })

  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)

  interface RegisterForm {
    username: string
    password: string
    confirmPassword: string
    agreement: boolean
  }

  const USERNAME_MIN_LENGTH = 3
  const USERNAME_MAX_LENGTH = 20
  const PASSWORD_MIN_LENGTH = 6
  const REDIRECT_DELAY = 1000

  const { t, locale } = useI18n()
  const router = useRouter()
  const formRef = ref<FormInstance>()

  const loading = ref(false)
  const formKey = ref(0)

  // 监听语言切换，重置表单
  watch(locale, () => {
    formKey.value++
  })

  const formData = reactive<RegisterForm>({
    username: '',
    password: '',
    confirmPassword: '',
    agreement: false
  })

  /**
   * 验证密码
   * 当密码输入后，如果确认密码已填写，则触发确认密码的验证
   */
  const validatePassword = (_rule: any, value: string, callback: (error?: Error) => void) => {
    if (!value) {
      callback(new Error(t('register.placeholder.password')))
      return
    }

    if (formData.confirmPassword) {
      formRef.value?.validateField('confirmPassword')
    }

    callback()
  }

  /**
   * 验证确认密码
   * 检查确认密码是否与密码一致
   */
  const validateConfirmPassword = (
    _rule: any,
    value: string,
    callback: (error?: Error) => void
  ) => {
    if (!value) {
      callback(new Error(t('register.rule.confirmPasswordRequired')))
      return
    }

    if (value !== formData.password) {
      callback(new Error(t('register.rule.passwordMismatch')))
      return
    }

    callback()
  }

  /**
   * 验证用户协议
   * 确保用户已勾选同意协议
   */
  const validateAgreement = (_rule: any, value: boolean, callback: (error?: Error) => void) => {
    if (!value) {
      callback(new Error(t('register.rule.agreementRequired')))
      return
    }
    callback()
  }

  const rules = computed<FormRules<RegisterForm>>(() => ({
    username: [
      { required: true, message: t('register.placeholder.username'), trigger: 'blur' },
      {
        min: USERNAME_MIN_LENGTH,
        max: USERNAME_MAX_LENGTH,
        message: t('register.rule.usernameLength'),
        trigger: 'blur'
      }
    ],
    password: [
      { required: true, validator: validatePassword, trigger: 'blur' },
      { min: PASSWORD_MIN_LENGTH, message: t('register.rule.passwordLength'), trigger: 'blur' }
    ],
    confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
    agreement: [{ validator: validateAgreement, trigger: 'change' }]
  }))

  /**
   * 注册用户
   * 验证表单后提交注册请求
   */
  const register = async () => {
    if (!formRef.value || loading.value) return

    try {
      // 先进行表单校验
      await formRef.value.validate()
      loading.value = true

      // 调用后端注册接口
      const params: Api.Auth.RegisterParams = {
        username: formData.username,
        password: formData.password
      }
      await fetchRegister(params)

      // 注册成功提示并跳转登录页
      ElMessage.success('注册成功')
      toLogin()
    } catch (error) {
      console.error('注册失败:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 跳转到登录页面
   */
  const toLogin = () => {
    setTimeout(() => {
      router.push({ name: 'Login' })
    }, REDIRECT_DELAY)
  }

  onMounted(async () => {
    const enabled = await isRegisterEnabled()
    if (!enabled) {
      router.replace({ name: 'Login' })
    }
  })
</script>

<style scoped>
  @import '../login/style.css';
</style>
