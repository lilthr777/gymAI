<template>
  <div class="app-layout">
    <!-- 顶部导航 -->
    <header class="topbar">
      <div class="topbar-left">
        <span class="topbar-brand">gymAI</span>
      </div>
      <div class="topbar-right">
        <button class="theme-toggle" @click="appStore.toggleTheme">
          <el-icon :size="18"><Moon v-if="!appStore.isDark" /><Sunny v-else /></el-icon>
        </button>
        <el-dropdown v-if="userStore.isLoggedIn()" trigger="click" placement="bottom-end">
          <button class="user-trigger">
            <el-avatar :size="28" icon="UserFilled" />
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">个人资料</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部导航栏 -->
    <nav v-if="showTabBar" class="tabbar">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="tabbar-item"
        :class="{ active: isTabActive(tab) }"
        @click="router.push(tab.path)"
      >
        <el-icon :size="20"><component :is="tab.icon" /></el-icon>
        <span class="tabbar-label">{{ tab.label }}</span>
      </div>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { Moon, Sunny } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const tabs = [
  { path: '/home', label: '首页', icon: 'HomeFilled' },
  { path: '/courses', label: '课程', icon: 'Calendar' },
  { path: '/coaches', label: '教练', icon: 'Avatar' },
  { path: '/ai-chat', label: 'AI助手', icon: 'ChatDotRound' },
  { path: '/profile', label: '我的', icon: 'User' },
]

const showTabBar = computed(() => route.meta.showTabBar !== false)

const isTabActive = (tab: { path: string }) => {
  if (tab.path === '/home') return route.path === '/home'
  if (tab.path === '/courses') return route.path.startsWith('/courses')
  if (tab.path === '/coaches') return route.path.startsWith('/coaches')
  if (tab.path === '/profile') return ['/profile', '/my-courses', '/my-checkins'].includes(route.path)
  return route.path === tab.path
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.app-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

// ── Topbar ───────────────────────────────────
.topbar {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: $color-sheet;
  border-bottom: 1px solid $color-steel;
  flex-shrink: 0;
  z-index: 10;
}

.topbar-brand {
  font-family: $font-display;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: $color-carbon;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.theme-toggle {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: $color-lead;
  cursor: pointer;
  border-radius: $radius-md;
  transition: color $transition-fast;

  &:hover {
    color: $color-cobalt;
  }
}

.user-trigger {
  display: flex;
  align-items: center;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 2px;
  border-radius: 50%;
  transition: box-shadow $transition-fast;

  &:hover {
    box-shadow: 0 0 0 2px $color-steel;
  }
}

// ── Content ──────────────────────────────────
.main-content {
  flex: 1;
  overflow-y: auto;
  background: $color-magnesium;
  padding-bottom: 0;
}

// ── TabBar ───────────────────────────────────
.tabbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: $color-sheet;
  border-top: 1px solid $color-steel;
  flex-shrink: 0;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  flex: 1;
  height: 100%;
  cursor: pointer;
  color: $color-lead;
  transition: color $transition-fast;
  padding: 4px 0;

  &:hover {
    color: $color-cobalt-light;
  }

  &.active {
    color: $color-cobalt;
  }
}

.tabbar-label {
  font-size: 10px;
  font-family: $font-body;
  line-height: 1;
}

// ── Dark Mode ────────────────────────────────
html.dark {
  .topbar {
    background: $dark-bg-secondary;
    border-bottom-color: $dark-border;
  }

  .topbar-brand {
    color: $dark-text;
  }

  .main-content {
    background: $dark-bg;
  }

  .tabbar {
    background: $dark-bg-secondary;
    border-top-color: $dark-border;
  }
}
</style>
