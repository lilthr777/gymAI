import type { App, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

function hasPermission(requiredRole: string): boolean {
  const userStore = useUserStore()
  return userStore.role === requiredRole
}

// v-permission: 权限控制指令，传入角色名，无权限则移除元素
const permission = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    if (value && !hasPermission(value)) {
      el.parentNode?.removeChild(el)
    }
  },
}

// v-focus: 自动聚焦
const focus = {
  mounted(el: HTMLElement) {
    const input = el.querySelector('input') || el
    ;(input as HTMLElement).focus()
  },
}

export function setupDirectives(app: App) {
  app.directive('permission', permission)
  app.directive('focus', focus)
}
