import { fetchCaptcha } from '@/api/auth'
import { logger } from '@/utils/sys/logger'

export type CaptchaScene = 'login' | 'forget-password'

/**
 * 图形验证码：拉取、刷新、错误处理（登录/忘记密码共用）
 */
export function useCaptcha(scene: CaptchaScene) {
  const captchaToken = ref('')
  const captchaImage = ref('')
  const loadingCaptcha = ref(false)
  const captchaError = ref('')

  const refreshCaptcha = async (onClearInput?: () => void) => {
    loadingCaptcha.value = true
    captchaError.value = ''

    try {
      const response = await fetchCaptcha(scene)
      captchaToken.value = response.captchaToken
      captchaImage.value = response.captchaImage
      onClearInput?.()
    } catch (error) {
      captchaToken.value = ''
      captchaImage.value = ''
      logger.error('获取验证码失败:', error)
    } finally {
      loadingCaptcha.value = false
    }
  }

  const handleCaptchaFailure = (message: string, onClearInput?: () => void) => {
    captchaError.value = message
    refreshCaptcha(onClearInput)
  }

  return {
    captchaToken,
    captchaImage,
    loadingCaptcha,
    captchaError,
    refreshCaptcha,
    handleCaptchaFailure
  }
}
