import { reactive } from 'vue'

export type SnackbarType = 'success' | 'info' | 'warning' | 'error'

export interface SnackbarOptions {
  message: string
  type?: SnackbarType
  duration?: number
}

export interface SnackbarItem extends Required<SnackbarOptions> {
  id: number
}

const DEFAULT_TYPE: SnackbarType = 'info'
const DEFAULT_DURATION = 3000

const items = reactive<SnackbarItem[]>([])
let nextId = 0

export function useSnackbar() {
  function showMessage(raw: SnackbarOptions | string): number {
    const options: SnackbarOptions = typeof raw === 'string' ? { message: raw } : raw
    const item: SnackbarItem = {
      id: nextId++,
      message: options.message,
      type: options.type ?? DEFAULT_TYPE,
      duration: options.duration ?? DEFAULT_DURATION,
    }

    items.push(item)

    if (item.duration > 0) {
      setTimeout(() => {
        removeItem(item.id)
      }, item.duration)
    }

    return item.id
  }

  function closeMessage(id: number) {
    removeItem(id)
  }

  return { showMessage, closeMessage }
}

function removeItem(id: number) {
  const idx = items.findIndex((i) => i.id === id)
  if (idx !== -1) items.splice(idx, 1)
}

/** 供 SnackbarContainer 绑定，不直接导出给外部调用方 */
export { items as _snackbarItems }
