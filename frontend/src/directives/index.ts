import type { App } from 'vue'

// v-permission: 权限控制指令
const permission = {
  mounted(el: HTMLElement, binding: any) {
    const { value } = binding
    if (value) {
      // 示例：根据角色控制显示
      // const userStore = useUserStore()
      // if (!hasPermission(value)) el.parentNode?.removeChild(el)
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
