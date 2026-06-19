<!-- 登录、注册、忘记密码左侧背景 -->
<template>
  <div class="login-left-view">
    <div class="logo">
      <ArtLogo class="icon" size="46" />
      <h1 class="title">{{ AppConfig.systemInfo.name }}</h1>
    </div>

    <div class="left-img">
      <ThemeSvg :src="loginIcon" size="100%" />
    </div>

    <div class="text-wrap">
      <h1> {{ t('login.leftView.title') }} </h1>
      <p> {{ t('login.leftView.subTitle') }} </p>
    </div>

    <!-- 几何装饰元素 -->
    <div class="geometric-decorations">
      <!-- 基础几何形状 -->
      <div class="geo-element circle-outline animate-fade-in-up" style="animation-delay: 0s"></div>
      <div
        class="geo-element square-rotated animate-fade-in-left"
        style="animation-delay: 0s"
      ></div>
      <div class="geo-element circle-small animate-fade-in-up" style="animation-delay: 0.3s"></div>

      <div
        class="geo-element square-bottom-right animate-fade-in-right"
        style="animation-delay: 0s"
      ></div>

      <!-- 背景泡泡 -->
      <div class="geo-element bg-bubble animate-scale-in" style="animation-delay: 0.5s"></div>

      <!-- 太阳/月亮 -->
      <div
        class="geo-element circle-top-right animate-fade-in-down"
        style="animation-delay: 0.5s"
        @click="themeAnimation"
      ></div>

      <!-- 装饰点 -->
      <div class="geo-element dot dot-top-left animate-bounce-in" style="animation-delay: 0s"></div>
      <div
        class="geo-element dot dot-top-right animate-bounce-in"
        style="animation-delay: 0s"
      ></div>
      <div
        class="geo-element dot dot-center-right animate-bounce-in"
        style="animation-delay: 0s"
      ></div>

      <!-- 叠加方块组 -->
      <div class="squares-group">
        <i
          class="geo-element square square-blue animate-fade-in-left-rotated-blue"
          style="animation-delay: 0.2s"
        ></i>
        <i
          class="geo-element square square-pink animate-fade-in-left-rotated-pink"
          style="animation-delay: 0.4s"
        ></i>
        <i
          class="geo-element square square-purple animate-fade-in-left-no-rotation"
          style="animation-delay: 0.6s"
        ></i>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import AppConfig from '@/config'
  import loginIcon from '@imgs/svg/login_icon.svg'
  import { themeAnimation } from '@/utils/ui/animation'

  const { t } = useI18n()

  // 定义 props
  defineProps<{
    hideContent?: boolean // 是否隐藏内容，只显示 logo
  }>()
</script>

<style lang="scss" scoped>
  // 颜色变量定义
  $primary-light-7: var(--el-color-primary-light-7);
  $primary-light-8: var(--el-color-primary-light-8);
  $primary-light-9: var(--el-color-primary-light-9);
  $primary-base: var(--el-color-primary);
  $main-bg: var(--default-box-color);

  // 混合颜色函数
  $bg-mix-light-9: color-mix(in srgb, $primary-light-9 100%, $main-bg);
  $bg-mix-light-8: color-mix(in srgb, $primary-light-8 80%, $main-bg);
  $bg-mix-light-7: color-mix(in srgb, $primary-light-7 80%, $main-bg);

  .login-left-view {
    position: relative;
    box-sizing: border-box;
    width: 65vw;
    height: 100%;
    padding: 15px;
    overflow: hidden;
    background: linear-gradient(
      135deg,
      $bg-mix-light-9 0%,
      color-mix(in srgb, $primary-light-8 50%, $main-bg) 100%
    );

    // 添加背景纹理
    &::before {
      position: absolute;
      inset: 0;
      pointer-events: none;
      content: '';
      background-image:
        radial-gradient(circle at 20% 80%, rgb(255 255 255 / 10%) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgb(255 255 255 / 8%) 0%, transparent 40%);
    }

    .logo {
      position: relative;
      z-index: 100;
      display: flex;
      align-items: center;
      animation: logoFadeIn 0.8s ease-out forwards;

      .title {
        margin-left: 10px;
        font-size: 20px;
        font-weight: 500;
        letter-spacing: 0.5px;
        background: linear-gradient(135deg, var(--art-gray-800), var(--art-gray-600));
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }

    .left-img {
      position: absolute;
      inset: 0 0 10.5%;
      z-index: 10;
      width: 40%;
      margin: auto;
      animation:
        slideInLeft 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards,
        floatAnimation 6s ease-in-out infinite 0.6s;
    }

    .text-wrap {
      position: absolute;
      bottom: 80px;
      width: 100%;
      text-align: center;
      animation: slideInLeft 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;

      h1 {
        margin-bottom: 12px;
        font-size: 26px;
        font-weight: 500;
        color: var(--art-gray-900) !important;
        letter-spacing: 1px;
      }

      p {
        margin-top: 10px;
        font-size: 15px;
        line-height: 1.6;
        color: var(--art-gray-600) !important;
      }
    }

    .geometric-decorations {
      .geo-element {
        position: absolute;
        opacity: 0;
        animation-fill-mode: forwards;
        animation-duration: 0.8s;
        animation-timing-function: cubic-bezier(0.25, 0.46, 0.45, 0.94);
      }

      // 动画 mixin
      @mixin fadeAnimation($direction: '', $rotation: 0deg) {
        from {
          opacity: 0;

          @if $direction == 'up' {
            transform: translateY(30px) rotate($rotation);
          } @else if $direction == 'down' {
            transform: translateY(-30px) rotate($rotation);
          } @else if $direction == 'left' {
            transform: translateX(-30px) rotate($rotation);
          } @else if $direction == 'right' {
            transform: translateX(30px) rotate($rotation);
          }
        }

        to {
          opacity: 1;

          @if $direction == 'up' or $direction == 'down' {
            transform: translateY(0) rotate($rotation);
          } @else {
            transform: translateX(0) rotate($rotation);
          }
        }
      }

      // 动画定义
      @keyframes fadeInUp {
        @include fadeAnimation('up');
      }

      @keyframes fadeInDown {
        @include fadeAnimation('down');
      }

      @keyframes fadeInLeft {
        @include fadeAnimation('left');
      }

      @keyframes fadeInLeftRotated {
        @include fadeAnimation('left', -25deg);
      }

      @keyframes fadeInRight {
        @include fadeAnimation('right');
      }

      @keyframes fadeInRightRotated {
        @include fadeAnimation('right', 45deg);
      }

      @keyframes fadeInLeftRotatedBlue {
        @include fadeAnimation('left', -10deg);
      }

      @keyframes fadeInLeftRotatedPink {
        @include fadeAnimation('left', 10deg);
      }

      @keyframes fadeInLeftNoRotation {
        @include fadeAnimation('left');
      }

      @keyframes scaleIn {
        from {
          opacity: 0;
          transform: scale(0.8);
        }

        to {
          opacity: 1;
          transform: scale(1);
        }
      }

      @keyframes bounceIn {
        0% {
          opacity: 0;
          transform: scale(0.3);
        }

        50% {
          opacity: 1;
          transform: scale(1.05);
        }

        70% {
          transform: scale(0.9);
        }

        100% {
          opacity: 1;
          transform: scale(1);
        }
      }

      @keyframes lineGrow {
        from {
          opacity: 0;
        }

        to {
          opacity: 1;
        }
      }

      @keyframes slideInLeft {
        from {
          opacity: 0;
          transform: translateX(-30px);
        }

        to {
          opacity: 1;
          transform: translateX(0);
        }
      }

      @keyframes logoFadeIn {
        from {
          opacity: 0;
          transform: translateY(-10px);
        }

        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      @keyframes floatAnimation {
        0%,
        100% {
          transform: translateY(0);
        }

        50% {
          transform: translateY(-15px);
        }
      }

      @keyframes pulseGlow {
        0%,
        100% {
          box-shadow: 0 0 20px rgba(var(--el-color-primary-rgb), 0.2);
        }

        50% {
          box-shadow: 0 0 40px rgba(var(--el-color-primary-rgb), 0.4);
        }
      }

      @keyframes rotateSlowly {
        from {
          transform: rotate(0deg);
        }

        to {
          transform: rotate(360deg);
        }
      }

      // 动画类
      .animate-fade-in-up {
        animation-name: fadeInUp;
      }

      .animate-fade-in-down {
        animation-name: fadeInDown;
      }

      .animate-fade-in-left {
        animation-name: fadeInLeft;
      }

      .animate-fade-in-right {
        animation-name: fadeInRight;
      }

      .animate-scale-in {
        animation-name: scaleIn;
        animation-duration: 1.2s;
      }

      .animate-bounce-in {
        animation-name: bounceIn;
        animation-duration: 0.6s;
      }

      .animate-fade-in-left-rotated-blue {
        animation-name: fadeInLeftRotatedBlue;
      }

      .animate-fade-in-left-rotated-pink {
        animation-name: fadeInLeftRotatedPink;
      }

      .animate-fade-in-left-no-rotation {
        animation-name: fadeInLeftNoRotation;
      }

      // 基础几何形状
      .circle-outline {
        top: 10%;
        left: 25%;
        width: 42px;
        height: 42px;
        border: 2px solid $primary-light-8;
        border-radius: 50%;
        animation:
          fadeInUp 0.8s ease-out forwards,
          pulseGlow 3s ease-in-out infinite 0.8s;
      }

      .square-rotated {
        top: 50%;
        left: 16%;
        width: 60px;
        height: 60px;
        background: linear-gradient(135deg, $bg-mix-light-8, $bg-mix-light-7);
        border-radius: 8px;
        box-shadow: 0 8px 24px rgb(0 0 0 / 6%);

        &.animate-fade-in-left {
          animation-name: fadeInLeftRotated;
        }
      }

      .circle-small {
        bottom: 26%;
        left: 30%;
        width: 18px;
        height: 18px;
        background: linear-gradient(135deg, $primary-light-8, $primary-light-7);
        border-radius: 50%;
        box-shadow: 0 4px 12px rgb(0 0 0 / 8%);
      }

      // 太阳/月亮效果
      .circle-top-right {
        top: 3%;
        right: 3%;
        z-index: 100;
        width: 50px;
        height: 50px;
        cursor: pointer;
        background: linear-gradient(135deg, $bg-mix-light-7, $bg-mix-light-8);
        border-radius: 50%;
        box-shadow: 0 4px 20px rgb(0 0 0 / 10%);
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

        &::after {
          position: absolute;
          top: 50%;
          left: 50%;
          width: 100%;
          height: 100%;
          content: '';
          background: linear-gradient(135deg, #fcbb04, #fffc00);
          border-radius: 50%;
          opacity: 0;
          transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
          transform: translate(-50%, -50%);
        }

        &:hover {
          box-shadow: 0 0 40px rgb(255 252 0 / 50%);
          transform: scale(1.1);

          &::after {
            opacity: 1;
          }
        }
      }

      .square-bottom-right {
        right: 10%;
        bottom: 10%;
        width: 50px;
        height: 50px;
        background: linear-gradient(135deg, $primary-light-8, $primary-light-9);
        border-radius: 10px;
        box-shadow: 0 8px 24px rgb(0 0 0 / 6%);

        &.animate-fade-in-right {
          animation-name: fadeInRightRotated;
        }
      }

      // 背景泡泡
      .bg-bubble {
        top: -120px;
        right: -120px;
        width: 360px;
        height: 360px;
        background: linear-gradient(135deg, $bg-mix-light-8, $bg-mix-light-9);
        border-radius: 50%;
        opacity: 0.6;
      }

      // 装饰点
      .dot {
        width: 14px;
        height: 14px;
        background: linear-gradient(135deg, $primary-light-7, $primary-light-8);
        border-radius: 50%;
        box-shadow: 0 4px 12px rgb(0 0 0 / 10%);

        &.dot-top-left {
          top: 140px;
          left: 100px;
        }

        &.dot-top-right {
          top: 140px;
          right: 120px;
        }

        &.dot-center-right {
          top: 46%;
          right: 22%;
          background: linear-gradient(135deg, $primary-light-8, $primary-light-9);
        }
      }

      // 叠加方块组
      .squares-group {
        position: absolute;
        bottom: 18px;
        left: 20px;
        width: 140px;
        height: 140px;
        pointer-events: none;

        .square {
          position: absolute;
          display: block;
          border-radius: 12px;
          box-shadow: 0 12px 32px rgb(64 87 167 / 15%);
          transition:
            transform 0.3s ease,
            box-shadow 0.3s ease;

          &:hover {
            box-shadow: 0 16px 40px rgb(64 87 167 / 20%);
            transform: translateY(-4px) scale(1.05);
          }

          &.square-blue {
            top: 12px;
            left: 30px;
            z-index: 2;
            width: 50px;
            height: 50px;
            background: linear-gradient(
              135deg,
              rgb(from $primary-base r g b / 35%),
              rgb(from $primary-base r g b / 25%)
            );
            backdrop-filter: blur(4px);
          }

          &.square-pink {
            top: 30px;
            left: 48px;
            z-index: 1;
            width: 70px;
            height: 70px;
            background: linear-gradient(
              135deg,
              rgb(from $primary-base r g b / 18%),
              rgb(from $primary-base r g b / 12%)
            );
            backdrop-filter: blur(4px);
          }

          &.square-purple {
            top: 66px;
            left: 86px;
            z-index: 3;
            width: 32px;
            height: 32px;
            background: linear-gradient(
              135deg,
              rgb(from $primary-base r g b / 50%),
              rgb(from $primary-base r g b / 40%)
            );
            backdrop-filter: blur(4px);
          }
        }

        // 装饰线条
        &::after {
          position: absolute;
          top: 86px;
          left: 72px;
          width: 80px;
          height: 2px;
          content: '';
          background: linear-gradient(90deg, var(--el-color-primary-light-6), transparent);
          border-radius: 1px;
          opacity: 0;
          transform: rotate(50deg);
          animation: lineGrow 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
          animation-delay: 1.2s;
        }
      }
    }

    @media only screen and (width <= 1600px) {
      width: 60vw;

      .text-wrap {
        bottom: 40px;
      }
    }

    @media only screen and (width <= 1180px) {
      width: auto;
      height: auto;
      padding: 0;
      // 隐藏背景和其他内容，只保留 logo
      background: transparent;

      .left-img,
      .text-wrap,
      .geometric-decorations {
        display: none;
      }

      .logo {
        display: none;
      }
    }
  }

  // 暗色主题
  .dark .login-left-view {
    background:
      radial-gradient(ellipse at 0% 0%, rgb(99 102 241 / 15%) 0%, transparent 50%),
      radial-gradient(ellipse at 100% 100%, rgb(139 92 246 / 10%) 0%, transparent 50%),
      linear-gradient(
        135deg,
        color-mix(in srgb, $primary-light-9 60%, #070707) 0%,
        color-mix(in srgb, $primary-light-8 30%, #0a0a0a) 100%
      );

    // 暗色背景纹理
    &::before {
      background-image:
        radial-gradient(circle at 20% 80%, rgb(99 102 241 / 5%) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgb(139 92 246 / 3%) 0%, transparent 40%);
    }

    @media only screen and (width <= 1180px) {
      background: transparent;
    }

    .logo .title {
      background: linear-gradient(135deg, var(--art-gray-200), var(--art-gray-400));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .text-wrap {
      h1 {
        color: var(--art-gray-100) !important;
      }

      p {
        color: var(--art-gray-400) !important;
      }
    }

    .geometric-decorations {
      // 月亮效果
      .circle-top-right {
        background: linear-gradient(135deg, $bg-mix-light-8, $bg-mix-light-9);
        box-shadow:
          0 0 25px #333 inset,
          0 4px 20px rgb(0 0 0 / 30%);
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1) 0.1s;
        rotate: -48deg;

        &::before {
          position: absolute;
          top: 0;
          left: 15px;
          width: 50px;
          height: 50px;
          content: '';
          background: linear-gradient(135deg, $bg-mix-light-9, #0a0a0a);
          border-radius: 50%;
          transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }

        &:hover {
          background: transparent;
          box-shadow:
            0 40px 25px #ddd inset,
            0 0 30px rgb(255 255 255 / 20%);
          transform: scale(1.1);

          &::before {
            left: 18px;
          }

          &::after {
            opacity: 0;
          }
        }
      }

      .bg-bubble {
        background:
          radial-gradient(circle at 30% 30%, rgb(99 102 241 / 20%) 0%, transparent 60%),
          linear-gradient(
            135deg,
            $bg-mix-light-9,
            color-mix(in srgb, $primary-light-8 40%, #0a0a0a)
          );
        opacity: 0.5;
      }

      // 其他元素颜色调整
      .square-rotated {
        background: linear-gradient(
          135deg,
          $bg-mix-light-9,
          color-mix(in srgb, $primary-light-8 50%, #0a0a0a)
        );
        box-shadow: 0 8px 24px rgb(0 0 0 / 30%);
      }

      .circle-small,
      .dot {
        background: linear-gradient(135deg, $primary-light-8, $primary-light-9);
        box-shadow: 0 4px 12px rgb(0 0 0 / 20%);
      }

      .square-bottom-right {
        background: linear-gradient(
          135deg,
          $primary-light-9,
          color-mix(in srgb, $primary-light-8 50%, #0a0a0a)
        );
        box-shadow: 0 8px 24px rgb(0 0 0 / 30%);
      }

      .dot.dot-top-right {
        background: linear-gradient(135deg, $primary-light-8, $primary-light-9);
      }

      .circle-outline {
        border-color: $primary-light-7;
      }
    }

    // 方块组暗色调整
    .squares-group {
      .square {
        box-shadow: 0 12px 32px rgb(0 0 0 / 40%);

        &:hover {
          box-shadow: 0 16px 40px rgb(0 0 0 / 50%);
        }

        &.square-blue {
          background: linear-gradient(
            135deg,
            rgb(from $primary-base r g b / 22%),
            rgb(from $primary-base r g b / 15%)
          );
        }

        &.square-pink {
          background: linear-gradient(
            135deg,
            rgb(from $primary-base r g b / 12%),
            rgb(from $primary-base r g b / 8%)
          );
        }

        &.square-purple {
          background: linear-gradient(
            135deg,
            rgb(from $primary-base r g b / 28%),
            rgb(from $primary-base r g b / 20%)
          );
        }
      }

      &::after {
        background: linear-gradient(90deg, $primary-light-7, transparent);
      }
    }
  }
</style>
