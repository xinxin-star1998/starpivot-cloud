<!-- 微信 OAuth 回调 -->
<template>
  <div v-loading="loading" class="wechat-callback">
    <p>{{ message }}</p>
  </div>
</template>

<script setup lang="ts">
import {
  consumePortalWechatOAuthContext,
  fetchPortalBindWechat,
  fetchPortalWechatLogin,
  fetchPortalWechatRegister
} from '@/api/portal/auth'
import {usePortalMemberStore} from '@/store/modules/portal-member'
import {ElMessage} from 'element-plus'

defineOptions({ name: 'PortalWechatCallback' })

  const route = useRoute()
  const router = useRouter()
  const portalStore = usePortalMemberStore()

  const loading = ref(true)
  const message = ref('正在处理微信授权...')

  onMounted(async () => {
    const code = route.query.code as string
    const state = route.query.state as string

    if (!code || !state) {
      message.value = '授权参数缺失'
      loading.value = false
      ElMessage.error('微信授权失败')
      return
    }

    const { mode, redirect } = consumePortalWechatOAuthContext()

    try {
      if (mode === 'bind') {
        await fetchPortalBindWechat({ code, state })
        ElMessage.success('微信绑定成功')
        await router.replace(redirect)
        return
      }

      const result =
        mode === 'register'
          ? await fetchPortalWechatRegister({ code, state })
          : await fetchPortalWechatLogin({ code, state })
      portalStore.setLogin(result)
      ElMessage.success(mode === 'register' ? '注册成功' : '登录成功')
      await router.replace(redirect)
    } catch {
      message.value = '授权处理失败，请重试'
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  .wechat-callback {
    min-height: 240px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--portal-text-secondary);
  }
</style>
