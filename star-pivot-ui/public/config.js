/**
 * 前端运行时配置（部署后可直接修改此文件，无需重新打包）
 *
 * VITE_API_URL 须与打包时 .env.production 一致（推荐 /api/v1），
 * 业务 API 模块内 url 写 /api/xxx，由 axios baseURL 拼接为 /api/v1/xxx。
 *
 * 生产部署示例：
 *   window.__APP_RUNTIME_CONFIG__.VITE_API_URL = '/api/v1';
 * 或直连后端：
 *   window.__APP_RUNTIME_CONFIG__.VITE_API_URL = 'http://你的服务器IP:8080/api/v1';
 */
window.__APP_RUNTIME_CONFIG__ = window.__APP_RUNTIME_CONFIG__ || {}
// window.__APP_RUNTIME_CONFIG__.VITE_API_URL = '/api/v1'
