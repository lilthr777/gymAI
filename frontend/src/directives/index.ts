import type { App } from 'vue'

// v-focus: 自动聚焦
const focus = {
  mounted(el: HTMLElement) {
    const input = el.querySelector('input') || el
    ;(input as HTMLElement).focus()
  },
}

export function setupDirectives(app: App) {
  app.directive('focus', focus)
}
