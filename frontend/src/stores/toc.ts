import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { HeadingItem } from '@/utils/toc'

export const useTocStore = defineStore('toc', () => {
  const headings = ref<HeadingItem[]>([])
  const activeId = ref('')

  function setHeadings(items: HeadingItem[]) {
    headings.value = items
    activeId.value = ''
  }

  function setActiveId(id: string) {
    activeId.value = id
  }

  function clear() {
    headings.value = []
    activeId.value = ''
  }

  return { headings, activeId, setHeadings, setActiveId, clear }
})
