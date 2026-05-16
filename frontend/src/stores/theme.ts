import { ref } from 'vue'
import { defineStore } from 'pinia'

const STORAGE_KEY = 'theme'

export const useThemeStore = defineStore('theme', () => {
  let saved: string | null = null
  try {
    saved = localStorage.getItem(STORAGE_KEY)
  } catch {
    // localStorage 不可用
  }

  const prefersDark = globalThis.matchMedia('(prefers-color-scheme: dark)')
  const isDark = ref(saved !== null ? saved === 'dark' : prefersDark.matches)

  document.documentElement.dataset.theme = isDark.value ? 'dark' : 'light'

  function applyTheme() {
    const theme = isDark.value ? 'dark' : 'light'
    document.documentElement.dataset.theme = theme
    try {
      localStorage.setItem(STORAGE_KEY, theme)
    } catch {
      // 存储配额超限
    }
  }

  function toggleTheme() {
    isDark.value = !isDark.value
    applyTheme()
  }

  function handleSystemChange(e: MediaQueryListEvent) {
    if (localStorage.getItem(STORAGE_KEY) !== null) return
    isDark.value = e.matches
    document.documentElement.dataset.theme = e.matches ? 'dark' : 'light'
  }

  prefersDark.addEventListener('change', handleSystemChange)

  return { isDark, toggleTheme }
})
