<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { ElMessageBox } from 'element-plus'
import { ArrowLeft, HomeFilled, SwitchButton, Expand, Fold } from '@element-plus/icons-vue'

interface MenuItem {
  path: string
  title: string
  icon: any
}

const props = defineProps<{
  menuItems: MenuItem[]
  title: string
}>()

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)
const isCollapse = ref(false)
const sidebarVisible = ref(false)
const windowWidth = ref(window.innerWidth)

const isMobile = computed(() => windowWidth.value < 768)

function onResize() {
  windowWidth.value = window.innerWidth
  if (!isMobile.value) {
    sidebarVisible.value = false
  }
}

onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

function toggleSidebar() {
  if (isMobile.value) {
    sidebarVisible.value = !sidebarVisible.value
  } else {
    isCollapse.value = !isCollapse.value
  }
}

function handleMenuSelect(index: string) {
  router.push(index)
  if (isMobile.value) sidebarVisible.value = false
}

function goToC端() {
  router.push('/home')
  if (isMobile.value) sidebarVisible.value = false
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
    })
  } catch { return }
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="backend-layout" :class="{ dark: appStore.isDark, mobile: isMobile }">
    <!-- 手机端遮罩 -->
    <div v-if="isMobile && sidebarVisible" class="sidebar-overlay" @click="sidebarVisible = false" />

    <!-- 左侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapse, visible: sidebarVisible }">
      <div class="sidebar-header">
        <span v-show="!isCollapse" class="logo-text">gymAI</span>
        <span v-show="isCollapse && !isMobile" class="logo-text-short">G</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse && !isMobile"
        :collapse-transition="false"
        background-color="transparent"
        text-color="var(--sidebar-text)"
        active-text-color="#0071e3"
        @select="handleMenuSelect"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-menu
          :collapse="isCollapse && !isMobile"
          background-color="transparent"
          text-color="var(--sidebar-text)"
        >
          <el-menu-item index="/home" @click="goToC端">
            <el-icon><HomeFilled /></el-icon>
            <template #title>C端首页</template>
          </el-menu-item>
        </el-menu>
      </div>
    </aside>

    <!-- 右侧主区域 -->
    <div class="main-area">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <el-button :icon="isMobile ? Expand : ArrowLeft" text @click="toggleSidebar" />
          <h1 class="page-title">{{ props.title }}</h1>
        </div>
        <div class="topbar-right">
          <el-switch
            v-model="appStore.isDark"
            inline-prompt
            :active-icon="'🌙'"
            :inactive-icon="'☀️'"
            size="small"
          />
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="isMobile ? 28 : 32" :src="userStore.avatar" />
              <span v-if="!isMobile" class="nickname">{{ userStore.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <span>{{ userStore.role === 'ADMIN' ? '管理员' : userStore.role === 'COACH' ? '教练' : '会员' }}</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
.backend-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-primary, #f5f5f7);
  color: var(--text-primary, #1d1d1f);
}

// 手机端遮罩
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 998;
}

// 侧边栏
.sidebar {
  width: 220px;
  min-width: 220px;
  background: var(--sidebar-bg, #ffffff);
  border-right: 1px solid var(--border-color, #e5e5ea);
  display: flex;
  flex-direction: column;
  transition: width 0.3s, min-width 0.3s, transform 0.3s;
  z-index: 999;

  &.collapsed {
    width: 64px;
    min-width: 64px;
  }

  .sidebar-header {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid var(--border-color, #e5e5ea);

    .logo-text { font-size: 20px; font-weight: 700; letter-spacing: -0.5px; }
    .logo-text-short { font-size: 20px; font-weight: 700; }
  }

  .sidebar-footer {
    margin-top: auto;
    border-top: 1px solid var(--border-color, #e5e5ea);
  }

  :deep(.el-menu) { border-right: none; }
  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    font-size: 14px;
    &.is-active { background-color: rgba(0, 113, 227, 0.08); }
  }
}

// 主区域
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.topbar {
  height: 56px;
  min-height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: var(--topbar-bg, rgba(255, 255, 255, 0.8));
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid var(--border-color, #e5e5ea);

  .topbar-left {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .topbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      .nickname { font-size: 14px; }
    }
  }
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

// 暗色模式
.dark {
  --sidebar-bg: #1c1c1e;
  --sidebar-text: #98989d;
  --border-color: #38383a;
  --bg-primary: #000000;
  --text-primary: #f5f5f7;
  --topbar-bg: rgba(28, 28, 30, 0.8);

  .sidebar-header .logo-text { color: #f5f5f7; }
}

// 过渡动画
.fade-slide-enter-active, .fade-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.fade-slide-enter-from { opacity: 0; transform: translateY(6px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-6px); }

// ========== 手机端适配 ==========
@media (max-width: 767px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    transform: translateX(-100%);

    &.visible {
      transform: translateX(0);
      box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
    }
  }

  .content {
    padding: 16px;
  }
}
</style>
