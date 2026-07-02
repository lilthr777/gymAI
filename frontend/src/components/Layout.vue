<template>
  <div class="app-layout">
    <!-- Top navigation — frosted glass -->
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
            <el-avatar :size="28" icon="UserFilled" :src="userStore.avatar" />
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="userStore.isAdmin" @click="router.push('/admin/dashboard')">
                ⚙️ 管理后台
              </el-dropdown-item>
              <el-dropdown-item v-if="userStore.isCoach" @click="router.push('/coach/dashboard')">
                📅 教练后台
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/profile')">个人资料</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- Content area -->
    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <Transition :name="route.meta.transition || 'fade'" mode="out-in">
          <component :is="Component" :key="route.path" />
        </Transition>
      </router-view>
    </main>

    <!-- Bottom tab bar -->
    <nav v-if="showTabBar" class="tabbar">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="tabbar-item"
        :class="{ active: isTabActive(tab) }"
        @click="router.push(tab.path)"
      >
        <el-icon :size="22"><component :is="tab.icon" /></el-icon>
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
  { path: '/ai-chat', label: 'AI', icon: 'ChatDotRound' },
  { path: '/profile', label: '我的', icon: 'User' },
]

const showTabBar = computed(() => route.meta.showTabBar !== false)

const isTabActive = (tab: { path: string }) => {
  if (tab.path === '/home') return route.path === '/home'
  if (tab.path === '/profile') return ['/profile', '/my-courses', '/my-checkins', '/card', '/edit-profile'].includes(route.path)
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
  position: relative;
}

// ── Topbar — Apple frosted glass (fixed overlay) ──
.topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.topbar-brand {
  font-size: 19px;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: $color-text-primary;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-toggle {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: $color-text-secondary;
  cursor: pointer;
  border-radius: 50%;
  transition: color $transition-fast;

  &:hover {
    color: $color-accent;
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
    box-shadow: 0 0 0 2px $color-border;
  }
}

// ── Content (scrolls under frosted bars) ───
.main-content {
  flex: 1;
  overflow-y: auto;
  background: $color-bg;
  padding-top: 48px;
  padding-bottom: 50px;
}

// ── TabBar — iOS-style fixed bottom ─────────
.tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding-bottom: env(safe-area-inset-bottom, 0);
  z-index: 100;
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
  flex: 1;
  height: 100%;
  cursor: pointer;
  color: $color-text-tertiary;
  transition: color $transition-fast;

  &:hover {
    color: $color-text-secondary;
  }

  &.active {
    color: $color-accent;
  }
}

.tabbar-label {
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.01em;
  line-height: 1;
}

// ── Dark Mode ───────────────────────────────
html.dark {
  .topbar {
    background: rgba(28, 28, 30, 0.72);
    border-bottom-color: rgba(255, 255, 255, 0.08);
  }

  .topbar-brand {
    color: $dark-text;
  }

  .main-content {
    background: $dark-bg;
  }

  .tabbar {
    background: rgba(28, 28, 30, 0.72);
    border-top-color: rgba(255, 255, 255, 0.08);
  }
}
</style>
