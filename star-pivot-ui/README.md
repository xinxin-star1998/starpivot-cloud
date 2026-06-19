# Star Pivot UI

> Star Pivot UI - 一个基于 Vue 3 + TypeScript + Vite + Element Plus 的现代化后台管理系统模板

## ✨ 特性

- 🎨 **现代化设计** - 精美的 UI 设计，支持亮色/暗色主题切换
- 🚀 **技术栈** - Vue 3 + TypeScript + Vite + Element Plus
- 📦 **开箱即用** - 完善的权限管理、路由配置、状态管理
- 🎯 **类型安全** - 完整的 TypeScript 类型定义
- 🌍 **国际化** - 支持中英文切换
- 📱 **响应式** - 适配移动端和桌面端
- 🎨 **组件丰富** - 丰富的核心组件库（表单、图表、表格等）
- 🔒 **权限管理** - 完善的路由权限和指令权限系统
- 💾 **数据持久化** - Store 数据自动持久化到 localStorage
- 📝 **代码规范** - ESLint + Prettier + Stylelint
- 🔧 **Git 规范** - Husky + Commitlint + Commitizen

## 📋 环境要求

- Node.js >= 20.19.0
- pnpm >= 8.8.0

## 🚀 快速开始

### 安装依赖

```bash
pnpm install
```

### 配置环境变量

复制 `.env.example` 文件为 `.env`，并根据实际情况修改配置：

```bash
cp .env.example .env
```

### 启动开发服务器

```bash
pnpm dev
```

### 构建生产版本

```bash
pnpm build
```

### 预览生产构建

```bash
pnpm serve
```

## 📁 项目结构

```
star-pivot-ui/
├── src/
│   ├── api/              # API 接口层
│   ├── assets/           # 静态资源
│   ├── components/       # 组件库
│   ├── config/           # 配置文件
│   ├── directives/       # 自定义指令
│   ├── enums/            # 枚举定义
│   ├── hooks/            # 组合式函数
│   ├── locales/          # 国际化
│   ├── router/           # 路由配置
│   ├── store/            # 状态管理
│   ├── types/            # TypeScript 类型定义
│   ├── utils/            # 工具函数
│   └── views/            # 页面视图
├── public/               # 公共资源
├── .env                  # 环境变量配置
├── .env.example          # 环境变量模板
├── vite.config.ts        # Vite 配置
├── tsconfig.json         # TypeScript 配置
└── package.json          # 项目依赖
```

详细的项目结构说明请查看 [readed.md](./readed.md)

## 🛠️ 开发

### 代码检查

```bash
# ESLint 检查
pnpm lint

# ESLint 自动修复
pnpm fix

# Prettier 格式化
pnpm lint:prettier

# Stylelint 检查
pnpm lint:stylelint
```

### Git 提交

项目使用 Commitizen 进行规范的 Git 提交：

```bash
pnpm commit
```

## 📦 技术栈

- **框架**: Vue 3.5.21
- **语言**: TypeScript 5.6.3
- **构建工具**: Vite 7.1.5
- **UI 库**: Element Plus 2.11.2
- **状态管理**: Pinia 3.0.3
- **路由**: Vue Router 4.5.1
- **样式**: SCSS + Tailwind CSS 4.1.14
- **图表**: ECharts 6.0.0
- **国际化**: Vue I18n 9.14.0

## 📄 许可证

[MIT](./LICENSE)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

- 项目文档: https://www.artd.pro/docs/zh/
- GitHub: https://github.com/Daymychen/star-pivot-ui

---

Made with ❤️ by Art Design Pro Team
