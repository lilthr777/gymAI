<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="sidebar-brand">
        <span class="sidebar-logo">gymAI</span>
      </div>

      <nav class="sidebar-nav">
        <el-menu
          :default-active="route.path"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          router
        >
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-menu>
      </nav>

      <div class="sidebar-footer">
        <button class="collapse-trigger" @click="appStore.toggleSidebar">
          <el-icon :size="18"><Fold v-if="!appStore.sidebarCollapsed" /><Expand v-else /></el-icon>
        </button>
      </div>
    </aside>

    <!-- Main -->
    <div class="main-area">
      <!-- Header -->
      <header class="topbar">
        <div class="topbar-left">
          <el-breadcrumb separator="">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <span class="breadcrumb-sep">/</span>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="topbar-right">
          <button class="theme-toggle" @click="appStore.toggleTheme" :title="appStore.isDark ? '浅色模式' : '深色模式'">
            <el-icon :size="18"><Moon v-if="!appStore.isDark" /><Sunny v-else /></el-icon>
          </button>

          <el-dropdown trigger="click" placement="bottom-end">
            <button class="user-trigger">
              <el-avatar :size="30" icon="UserFilled" />
              <span class="user-name">{{ userStore.nickname || '管理员' }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Content -->
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { Moon, Sunny } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const menuItems = [
  { path: '/dashboard', title: '仪表盘', icon: 'Odometer' },
  { path: '/members', title: '会员管理', icon: 'User' },
  { path: '/coaches', title: '教练管理', icon: 'Avatar' },
  { path: '/courses', title: '课程管理', icon: 'Calendar' },
  { path: '/checkins', title: '签到记录', icon: 'Check' },
  { path: '/ai-chat', title: 'AI 助手', icon: 'ChatDotRound' },
]

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

// ── Sidebar ──────────────────────────────────
.sidebar {
  width: $sidebar-width;
  flex-shrink: 0;
  background: $color-carbon;
  display: flex;
  flex-direction: column;
  transition: width $transition-base;
  overflow: hidden;

  &.collapsed {
    width: $sidebar-collapsed-width;
  }
}

.sidebar-brand {
  height: $header-height;
  display: flex;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;

  .sidebar-logo {
    font-family: $font-display;
    font-size: 22px;
    font-weight: 600;
    letter-spacing: 0.04em;
    color: $color-sheet;
    white-space: nowrap;
  }
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;

  :deep(.el-menu) {
    border-right: none;
    background: transparent;

    .el-menu-item {
      margin: 2px 12px;
      border-radius: $radius-md;
      height: 40px;
      line-height: 40px;
      color: #9B9EA3;
      font-size: $font-size-base;
      font-family: $font-body;
      border-left: 2px solid transparent;
      transition: color $transition-fast, border-color $transition-fast, background $transition-fast;

      &:hover {
        color: #D0CCC5;
        background: rgba(255, 255, 255, 0.04);
      }

      &.is-active {
        color: $color-sheet;
        background: rgba(255, 255, 255, 0.06);
        border-left-color: $color-cobalt;
        border-radius: 0 $radius-md $radius-md 0;
        margin-left: 10px;
      }

      .el-icon {
        font-size: 18px;
      }
    }

    &.el-menu--collapse {
      .el-menu-item {
        margin: 2px 8px;
        border-left: none;
        justify-content: center;
        padding: 0 !important;

        &.is-active {
          border-left: none;
          margin-left: 8px;
          border-radius: $radius-md;
          background: rgba(255, 255, 255, 0.08);
        }
      }
    }
  }
}

.sidebar-footer {
  padding: 12px;
  flex-shrink: 0;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.collapse-trigger {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #6B6E73;
  cursor: pointer;
  border-radius: $radius-md;
  transition: color $transition-fast, background $transition-fast;

  &:hover {
    color: #D0CCC5;
    background: rgba(255, 255, 255, 0.04);
  }
}

// ── Main Area ────────────────────────────────
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

// ── Topbar ───────────────────────────────────
.topbar {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: $color-sheet;
  border-bottom: 1px solid $color-steel;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;

  :deep(.el-breadcrumb) {
    font-size: 13px;

    .el-breadcrumb__item {
      display: flex;
      align-items: center;
    }

    .el-breadcrumb__inner {
      color: $color-lead;
      font-weight: 400;
      transition: color $transition-fast;

      &:hover {
        color: $color-carbon;
      }
    }

    .el-breadcrumb__item:last-child .el-breadcrumb__inner {
      color: $color-carbon;
      font-weight: 500;
    }
  }
}

.breadcrumb-sep {
  margin: 0 8px;
  color: $color-steel;
  font-size: 13px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.theme-toggle {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid $color-steel;
  background: transparent;
  border-radius: $radius-md;
  color: $color-lead;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    border-color: $color-cobalt;
    color: $color-cobalt;
  }
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px 4px 4px;
  border: none;
  background: transparent;
  border-radius: $radius-md;
  cursor: pointer;
  transition: background $transition-fast;

  &:hover {
    background: rgba(0, 0, 0, 0.03);
  }

  .user-name {
    font-size: 13px;
    color: $color-ash;
    font-family: $font-body;
  }
}

// ── Content ──────────────────────────────────
.main-content {
  flex: 1;
  overflow-y: auto;
  background: $color-magnesium;
}

// ── Dark Mode ────────────────────────────────
html.dark {
  .topbar {
    background: $dark-bg-secondary;
    border-bottom-color: $dark-border;

    :deep(.el-breadcrumb) {
      .el-breadcrumb__inner {
        color: $dark-text-secondary;

        &:hover {
          color: $dark-text;
        }
      }

      .el-breadcrumb__item:last-child .el-breadcrumb__inner {
        color: $dark-text;
      }
    }
  }

  .breadcrumb-sep {
    color: $dark-border;
  }

  .theme-toggle {
    border-color: $dark-border;
    color: $dark-text-secondary;

    &:hover {
      border-color: $color-cobalt;
      color: $color-cobalt;
    }
  }

  .user-trigger {
    &:hover {
      background: rgba(255, 255, 255, 0.04);
    }

    .user-name {
      color: $dark-text;
    }
  }

  .main-content {
    background: $dark-bg;
  }
}
</style>
