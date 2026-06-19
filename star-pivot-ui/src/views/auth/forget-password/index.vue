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

          <div class="form-content">
            <div class="input-group">
              <span class="input-label" v-if="showInputLabel">账号</span>
              <ElInput
                class="custom-height"
                :placeholder="t('forgetPassword.placeholder')"
                v-model.trim="username"
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
            </div>

            <ElButton
              class="submit-btn custom-height"
              type="primary"
              @click="register"
              :loading="loading"
              v-ripple
            >
              {{ t('forgetPassword.submitBtnText') }}
            </ElButton>

            <ElButton class="back-btn custom-height" plain @click="toLogin">
              {{ t('forgetPassword.backBtnText') }}
            </ElButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { useSettingStore } from '@/store/modules/setting'

  defineOptions({ name: 'ForgetPassword' })

  const { t } = useI18n()
  const router = useRouter()
  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)
  const showInputLabel = ref(false)

  const username = ref('')
  const loading = ref(false)

  const register = async () => {}

  const toLogin = () => {
    router.push({ name: 'Login' })
  }
</script>

<style scoped>
  @import '../login/style.css';
</style>
