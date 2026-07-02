/** API 基址：本地开发指向网关，生产改为 HTTPS 域名 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

/** 开发 Mock 登录 code（与后端 mini-program.mock-code 一致） */
export const MOCK_MINI_LOGIN_CODE = 'mock_mini_code'

/** 是否使用 Mock 登录（微信开发者工具无真实 AppSecret 时） */
export const USE_MOCK_LOGIN = import.meta.env.VITE_USE_MOCK_LOGIN !== 'false'

export const STORAGE_TOKEN_KEY = 'sp-mp-token'
export const STORAGE_MEMBER_KEY = 'sp-mp-member'
