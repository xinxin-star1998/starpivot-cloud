/// <reference types="vite/client" />
import 'vue'
import type { Directive } from 'vue'

declare module '*.vue' {
  const component: import('vue').DefineComponent<
    Record<string, unknown>,
    Record<string, unknown>,
    unknown
  >
  export default component
}

declare module 'nprogress'

declare module 'crypto-js'

declare module 'vue-img-cutter'

declare module 'file-saver'

declare module '@wangeditor/editor-for-vue' {
  export const Editor: import('vue').DefineComponent<unknown, unknown, unknown>
  export const Toolbar: import('vue').DefineComponent<unknown, unknown, unknown>
}

declare module 'qrcode.vue' {
  export type Level = 'L' | 'M' | 'Q' | 'H'
  export type RenderAs = 'canvas' | 'svg'
  export type GradientType = 'linear' | 'radial'
  export interface ImageSettings {
    src: string
    height: number
    width: number
    excavate: boolean
  }
  export interface QRCodeProps {
    value: string
    size?: number
    level?: Level
    background?: string
    foreground?: string
    renderAs?: RenderAs
  }

  const QrcodeVue: import('vue').DefineComponent<QRCodeProps, object, unknown>
  export default QrcodeVue
}

// Element Plus 图标按需导入模块声明
// 某些情况下 @element-plus/icons-vue 的类型声明不会被 TS 正确识别，这里做一个兜底声明
declare module '@element-plus/icons-vue'

// Iconify / VueUse 在部分 TS 插件环境下可能无法解析，这里做兜底声明
declare module '@iconify/vue'
declare module '@vueuse/core'

// dayjs 时间库模块声明
// 项目已安装 dayjs，但可能缺少类型声明或 TS 未正确推断，这里做兜底声明
declare module 'dayjs'

// 运行时配置（部署后通过 public/config.js 注入，无需重新打包即可修改 API 地址）
interface AppRuntimeConfig {
  VITE_API_URL?: string
}
declare global {
  const __APP_VERSION__: string
  interface Window {
    __APP_RUNTIME_CONFIG__?: AppRuntimeConfig
  }
}

// Vue i18n 全局类型声明
declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $t: (key: string) => string
  }

  interface GlobalDirectives {
    ripple: Directive<HTMLElement, import('@/directives/business/ripple').RippleOptions | undefined>
  }
}
