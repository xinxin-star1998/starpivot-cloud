<!-- C 端会员登录 / 注册 -->
<template>
  <div class="portal-auth">
    <div class="portal-auth__card">
      <h1 class="portal-auth__title">StarPivot 商城</h1>
      <ElTabs v-model="activeTab">
        <ElTabPane label="登录" name="login">
          <ElForm ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent="handleLogin">
            <ElFormItem prop="account">
              <ElInput v-model="loginForm.account" placeholder="用户名 / 手机号" size="large" />
            </ElFormItem>
            <ElFormItem prop="password">
              <ElInput
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                size="large"
                show-password
              />
            </ElFormItem>
            <ElButton type="danger" size="large" class="submit-btn" :loading="loading" native-type="submit">
              登录
            </ElButton>
          </ElForm>
        </ElTabPane>

        <ElTabPane label="注册" name="register">
          <ElForm ref="registerFormRef" :model="registerForm" :rules="registerRules" @submit.prevent="handleRegister">
            <ElFormItem prop="username">
              <ElInput v-model="registerForm.username" placeholder="用户名" size="large" />
            </ElFormItem>
            <ElFormItem prop="mobile">
              <ElInput v-model="registerForm.mobile" placeholder="手机号（选填）" size="large" />
            </ElFormItem>
            <ElFormItem prop="password">
              <ElInput
                v-model="registerForm.password"
                type="password"
                placeholder="密码（6-32位）"
                size="large"
                show-password
              />
            </ElFormItem>
            <ElButton type="danger" size="large" class="submit-btn" :loading="loading" native-type="submit">
              注册
            </ElButton>
          </ElForm>
        </ElTabPane>
      </ElTabs>

      <ElButton link type="primary" class="back-link" @click="router.push('/portal')">← 返回商城首页</ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchPortalLogin, fetchPortalRegister } from '@/api/portal/member'
  import { usePortalMemberStore } from '@/store/modules/portal-member'
  import { ElMessage } from 'element-plus'

  defineOptions({ name: 'PortalLogin' })

  const route = useRoute()
  const router = useRouter()
  const portalStore = usePortalMemberStore()

  const activeTab = ref('login')
  const loading = ref(false)
  const loginFormRef = ref<FormInstance>()
  const registerFormRef = ref<FormInstance>()

  const loginForm = reactive({ account: '', password: '' })
  const registerForm = reactive({ username: '', mobile: '', password: '' })

  const loginRules: FormRules = {
    account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }

  const registerRules: FormRules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
    ]
  }

  function redirectAfterAuth() {
    const redirect = (route.query.redirect as string) || '/portal'
    router.replace(redirect)
  }

  async function handleLogin() {
    await loginFormRef.value?.validate()
    loading.value = true
    try {
      const result = await fetchPortalLogin(loginForm)
      portalStore.setLogin(result)
      ElMessage.success('登录成功')
      redirectAfterAuth()
    } finally {
      loading.value = false
    }
  }

  async function handleRegister() {
    await registerFormRef.value?.validate()
    loading.value = true
    try {
      await fetchPortalRegister(registerForm)
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      loginForm.account = registerForm.username
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    if (portalStore.isLogin) {
      redirectAfterAuth()
    }
  })
</script>

<style scoped lang="scss">
  .portal-auth {
    min-height: calc(100vh - 120px);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
  }

  .portal-auth__card {
    width: 100%;
    max-width: 420px;
    background: #fff;
    border-radius: 12px;
    padding: 32px 28px 24px;
    box-shadow: 0 8px 24px rgb(0 0 0 / 8%);
  }

  .portal-auth__title {
    text-align: center;
    margin: 0 0 24px;
    font-size: 24px;
    color: #e1251b;
  }

  .submit-btn {
    width: 100%;
    margin-top: 8px;
  }

  .back-link {
    display: block;
    margin: 16px auto 0;
  }
</style>
