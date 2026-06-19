<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="policy-wrap">
        <div class="policy-card">
          <h2 class="policy-title">{{ t('privacyPolicyPage.title') }}</h2>
          <p class="policy-update">{{ t('privacyPolicyPage.updateTime') }}</p>

          <div class="policy-content">
            <h3>{{ t('privacyPolicyPage.section1Title') }}</h3>
            <p>{{ t('privacyPolicyPage.section1Text') }}</p>

            <h3>{{ t('privacyPolicyPage.section2Title') }}</h3>
            <p>{{ t('privacyPolicyPage.section2Text') }}</p>

            <h3>{{ t('privacyPolicyPage.section3Title') }}</h3>
            <p>{{ t('privacyPolicyPage.section3Text') }}</p>

            <h3>{{ t('privacyPolicyPage.section4Title') }}</h3>
            <p>{{ t('privacyPolicyPage.section4Text') }}</p>
          </div>

          <div class="policy-footer">
            <ElButton type="primary" @click="toRegister">
              {{ t('privacyPolicyPage.backToRegister') }}
            </ElButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n'

  defineOptions({ name: 'PrivacyPolicy' })

  const { t } = useI18n()
  const router = useRouter()

  const toRegister = () => {
    router.push({ name: 'Register' })
  }
</script>

<style scoped>
  .policy-wrap {
    height: calc(100vh - 70px);
    padding: 24px;
    overflow: auto;
  }

  .policy-card {
    max-width: 760px;
    margin: 0 auto;
    padding: 28px;
    border-radius: 12px;
    background: var(--el-bg-color);
    box-shadow: 0 10px 30px rgb(15 23 42 / 8%);
  }

  .policy-title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    line-height: 1.3;
  }

  .policy-update {
    margin: 10px 0 0;
    color: var(--art-gray-600);
    font-size: 13px;
  }

  .policy-content {
    margin-top: 24px;
    color: var(--el-text-color-primary);
    line-height: 1.9;
    font-size: 14px;
  }

  .policy-content h3 {
    margin: 16px 0 8px;
    font-size: 16px;
    font-weight: 600;
  }

  .policy-content p {
    margin: 0;
  }

  .policy-footer {
    margin-top: 28px;
    display: flex;
    justify-content: flex-end;
  }
</style>
