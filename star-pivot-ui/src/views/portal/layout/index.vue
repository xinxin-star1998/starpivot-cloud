<!-- C 端商城布局 -->
<template>
  <div class="portal-layout">
    <PortalHeader />
    <main class="portal-main">
      <RouterView />
    </main>
    <footer class="portal-footer">
      <div class="portal-footer__inner">
        <div class="portal-footer__brand">
          <span class="portal-footer__logo">StarPivot</span>
          <p class="portal-footer__desc">精选好物，品质生活</p>
        </div>
        <div class="portal-footer__links">
          <div v-for="group in footerLinks" :key="group.title" class="portal-footer__group">
            <h4>{{ group.title }}</h4>
            <a
              v-for="link in group.links"
              :key="link.label"
              href="javascript:;"
              @click.prevent="link.action()"
            >
              {{ link.label }}
            </a>
          </div>
        </div>
      </div>
      <div class="portal-footer__bottom">
        <span>© {{ year }} StarPivot 商城 · C 端演示</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import PortalHeader from './portal-header.vue'

defineOptions({ name: 'PortalLayout' })

  const router = useRouter()
  const year = new Date().getFullYear()

  const footerLinks = [
    {
      title: '购物指南',
      links: [
        { label: '商城首页', action: () => router.push('/portal') },
        { label: '商品搜索', action: () => router.push('/portal/search') },
        { label: '优惠券', action: () => router.push('/portal/coupons') },
        { label: '我的订单', action: () => router.push('/portal/orders') }
      ]
    },
    {
      title: '会员服务',
      links: [
        { label: '会员中心', action: () => router.push('/portal/account') },
        { label: '我的收藏', action: () => router.push('/portal/account/favorites') },
        { label: '浏览足迹', action: () => router.push('/portal/account/history') },
        { label: '会员登录', action: () => router.push('/portal/login') },
        { label: '账号安全', action: () => router.push('/portal/account/security') },
        { label: '购物车', action: () => router.push('/portal/cart') }
      ]
    }
  ]
</script>

<style lang="scss">
  @import '../styles/variables.scss';
</style>

<style scoped lang="scss">
  .portal-layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: var(--portal-bg);
  }

  .portal-main {
    flex: 1;
    width: 100%;
    max-width: var(--portal-max-width);
    margin: 0 auto;
    padding: 20px 20px 32px;
    box-sizing: border-box;
  }

  .portal-footer {
    background: var(--portal-bg-elevated);
    border-top: 1px solid var(--portal-border);
    margin-top: auto;

    &__inner {
      max-width: var(--portal-max-width);
      margin: 0 auto;
      padding: 40px 20px 32px;
      display: flex;
      gap: 48px;
    }

    &__brand {
      flex-shrink: 0;
    }

    &__logo {
      font-size: 22px;
      font-weight: 800;
      background: var(--portal-primary-gradient);
      background-clip: text;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }

    &__desc {
      margin: 8px 0 0;
      font-size: 13px;
      color: var(--portal-text-secondary);
    }

    &__links {
      flex: 1;
      display: flex;
      gap: 48px;
    }

    &__group {
      h4 {
        margin: 0 0 12px;
        font-size: 14px;
        font-weight: 600;
        color: var(--portal-text);
      }

      a {
        display: block;
        margin-bottom: 8px;
        font-size: 13px;
        color: var(--portal-text-secondary);
        text-decoration: none;
        transition: color var(--portal-transition);

        &:hover {
          color: var(--portal-primary);
        }
      }
    }

    &__bottom {
      border-top: 1px solid var(--portal-border);
      text-align: center;
      padding: 16px 20px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }
  }

  @media (width <= 640px) {
    .portal-footer__inner {
      flex-direction: column;
      gap: 24px;
    }

    .portal-footer__links {
      flex-wrap: wrap;
      gap: 24px;
    }
  }
</style>
