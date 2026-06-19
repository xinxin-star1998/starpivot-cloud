/**
 * Console 工具函数
 * 提供统一的 console 输出，开发环境输出，生产环境自动移除
 */

/** 是否为开发环境 */
const isDev = import.meta.env.DEV

// ANSI 转义码生成网站  https://patorjk.com/software/taag/#p=display&f=Big&t=ABB%0A
const asciiArt = `
\x1b[32m欢迎使用 Star Pivot UI！
`

/**
 * 安全的 console.log（仅在开发环境输出）
 */
export const safeLog = (...args: unknown[]) => {
  if (isDev) {
    console.log(...args)
  }
}

/**
 * 安全的 console.warn（仅在开发环境输出）
 */
export const safeWarn = (...args: unknown[]) => {
  if (isDev) {
    console.warn(...args)
  }
}

/**
 * 安全的 console.error（仅在开发环境输出）
 */
export const safeError = (...args: unknown[]) => {
  if (isDev) {
    console.error(...args)
  }
}

/**
 * 安全的 console.info（仅在开发环境输出）
 */
export const safeInfo = (...args: unknown[]) => {
  if (isDev) {
    console.info(...args)
  }
}

// 启动时显示欢迎信息（仅在开发环境）
if (isDev) {
  console.log(asciiArt)
}
